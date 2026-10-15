package generador.infrastructure.export;

import generador.core.domain.classifier.*;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlParameter;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.model.UmlModel;
import generador.core.domain.relationship.*;
import generador.core.domain.spec.UmlModifier;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class UmlModelPlantUmlExporter {

    public void export(UmlModel model, Path outputPath) throws IOException {
        StringBuilder puml = new StringBuilder();
        puml.append("@startuml\n");
        puml.append("skinparam classAttributeIconSize 0\n");
        puml.append("skinparam linetype ortho\n\n");

        // ── 1. AGRUPAR CLASIFICADORES POR PAQUETE ──────────────────────────────
        //
        // El paquete de un clasificador se extrae de su qualifiedName tomando
        // únicamente los segmentos hasta la PRIMERA letra mayúscula que
        // corresponda a un tipo-raíz (primer segmento con mayúscula que NO
        // tenga padre en el mapa).  Para clases anidadas, todos comparten el
        // mismo paquete java, así que la llave se resuelve a nivel de paquete
        // de la CompilationUnit y NO incluye los nombres de las clases externas.
        //
        Map<String, List<UmlClassifier>> clasificadoresPorPaquete =
                agruparPorPaquete(model.classifiers().values());

        for (Map.Entry<String, List<UmlClassifier>> entry : clasificadoresPorPaquete.entrySet()) {
            String nombrePaquete = entry.getKey();
            List<UmlClassifier> listaClases = entry.getValue();

            puml.append("package \"").append(nombrePaquete).append("\" {\n");

            for (UmlClassifier classifier : listaClases) {

                String tipo        = obtenerTipoClassifier(classifier);
                String estereotipo = esRecord(classifier) ? " <<record>>" : "";
                String fullName    = classifier.qualifiedName();
                String alias       = sanitizarAlias(fullName);
                String simpleName  = obtenerNombreSimple(fullName);

                puml.append("  ").append(tipo).append(" \"").append(simpleName)
                    .append("\" as ").append(alias).append(estereotipo).append(" {\n");

                // ── Literales de enumeración ───────────────────────────────────
                if (classifier instanceof UmlEnumeration enumeration) {
                    for (UmlEnumerationLiteral literal : enumeration.literals()) {
                        // Las constantes de enum son publicas (+), static y readOnly en UML
                        puml.append("    + ").append(literal.name())
                            .append(" {static, readOnly}\n");
                    }
                    if (!enumeration.literals().isEmpty()
                            && (!classifier.properties().isEmpty()
                                    || !classifier.operations().isEmpty())) {
                        puml.append("    --\n");
                    }
                }

                // ── Propiedades / Atributos ────────────────────────────────────
                for (UmlProperty prop : classifier.properties()) {
                    puml.append("    ")
                        .append(convertirVisibilidad(prop.visibility()))
                        .append(" ")
                        .append(prop.name())
                        .append(" : ")
                        .append(prop.type().name());

                    // Valor inicial (ej: timeout = 5000)
                    prop.initialValue().ifPresent(val ->
                            puml.append(" = ").append(val));

                    // Modificadores UML como constraint
                    List<String> constraints = buildPropertyConstraints(prop);
                    if (!constraints.isEmpty())
                        puml.append(" {").append(String.join(", ", constraints)).append("}");

                    puml.append("\n");
                }

                boolean hayContenidoPrevio = !classifier.properties().isEmpty()
                        || (classifier instanceof UmlEnumeration en && !en.literals().isEmpty());

                if (hayContenidoPrevio && !classifier.operations().isEmpty()) {
                    puml.append("    --\n");
                }

                // ── Métodos / Operaciones ──────────────────────────────────────
                for (UmlOperation op : classifier.operations()) {
                    puml.append("    ")
                        .append(convertirVisibilidad(op.visibility()))
                        .append(" ")
                        .append(op.name())
                        .append("(")
                        .append(buildParameterList(op))
                        .append(") : ")
                        .append(op.isConstructor() ? "void" : op.returnType().name());

                    // Métodos abstractos / static
                    List<String> opConstraints = buildOperationConstraints(op);
                    if (!opConstraints.isEmpty())
                        puml.append(" {").append(String.join(", ", opConstraints)).append("}");

                    puml.append("\n");
                }

                puml.append("  }\n\n");
            }

            puml.append("}\n\n");
        }

        // ── 2. RELACIONES DE ANIDAMIENTO Y JERARQUIA (SOLO LA MAS FUERTE) ────
        //
        // Si entre dos clases existe mas de una relacion (ej. nesting y dependencia,
        // o asociacion y dependencia), conservamos unicamente la de mayor fuerza.
        //
        List<UmlRelationship> relacionesFiltradas = UmlRelationshipFilter.filterStrongest(model.relationships());
        Map<UmlRelationship, String> conectoresAsignados = asignarConectoresPorDestino(relacionesFiltradas);

        puml.append("' --- RELACIONES DE ANIDAMIENTO (CLASES INTERNAS) ---\n");

        for (UmlRelationship rel : relacionesFiltradas) {
            if (rel instanceof UmlNesting nesting) {
                String outerAlias = sanitizarAlias(nesting.outerClassifier().qualifiedName());
                String innerAlias = sanitizarAlias(nesting.innerClassifier().qualifiedName());
                String conector   = conectoresAsignados.getOrDefault(rel, "+--");
                // inner +-- outer  → el simbolo cruz aparece en el inner (izquierda)
                puml.append(innerAlias).append(" ").append(conector).append(" ").append(outerAlias).append("\n");
            }
        }

        puml.append("\n' --- RELACIONES DE HERENCIA E IMPLEMENTACION ---\n");

        // ── 3. HERENCIA Y REALIZACIÓN ──────────────────────────────────────────
        for (UmlRelationship rel : relacionesFiltradas) {
            if (rel instanceof UmlAssociation || rel instanceof UmlNesting) continue;

            String origenAlias  = sanitizarAlias(rel.source().qualifiedName());
            String destinoAlias = sanitizarAlias(rel.target().qualifiedName());
            String conector     = conectoresAsignados.get(rel);

            if (conector != null) {
                puml.append(origenAlias).append(" ")
                    .append(conector).append(" ")
                    .append(destinoAlias).append("\n");
            }
        }

        puml.append("\n@enduml\n");

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(puml.toString());
        }
    }

    // ── Helpers de contenido ──────────────────────────────────────────────────

    /** Construye la lista de parámetros formateada: "name : Type, name2 : Type2" */
    private String buildParameterList(UmlOperation op) {
        if (op.parameters().isEmpty()) return "";
        return op.parameters().stream()
                .map(p -> p.name() + " : " + p.type().name())
                .collect(Collectors.joining(", "));
    }

    /** Etiquetas UML de una propiedad según sus modificadores. */
    private List<String> buildPropertyConstraints(UmlProperty prop) {
        List<String> c = new ArrayList<>();
        if (prop.isStatic())   c.add("static");
        // FINAL se mapea a readOnly en UML
        if (prop.isFinal() || prop.isReadOnly()) c.add("readOnly");
        return c;
    }

    /** Etiquetas UML de una operación según sus modificadores. */
    private List<String> buildOperationConstraints(UmlOperation op) {
        List<String> c = new ArrayList<>();
        if (op.isStatic())   c.add("static");
        if (op.isAbstract()) c.add("abstract");
        return c;
    }

    // ── Helpers de clasificadores ─────────────────────────────────────────────

    private boolean esRecord(UmlClassifier classifier) {
        return classifier instanceof UmlRecord;
    }

    private String obtenerTipoClassifier(UmlClassifier classifier) {
        if (classifier instanceof UmlInterface)   return "interface";
        if (classifier instanceof UmlEnumeration) return "enum";
        if (classifier instanceof UmlAnnotation)  return "annotation";
        // abstract class: verificar explícitamente el modificador ABSTRACT
        if (classifier.modifiers().contains(UmlModifier.ABSTRACT)) return "abstract class";
        return "class";
    }

    private String convertirVisibilidad(Object visibilidad) {
        if (visibilidad == null) return "~";
        String vis = visibilidad.toString().toUpperCase();
        if (vis.contains("PUBLIC"))    return "+";
        if (vis.contains("PRIVATE"))   return "-";
        if (vis.contains("PROTECTED")) return "#";
        return "~";
    }

    // ── Agrupación por paquete ────────────────────────────────────────────────
    //
    // PROBLEMA ORIGINAL: Para "com::unimag::NestedProject::Module", la heurística
    // de "primera letra mayuscula" encontraba NestedProject como separador, creando
    // un paquete ficticio "com::unimag::NestedProject".
    //
    // SOLUCION: El paquete de un clasificador es el prefijo del nombre cualificado
    // de su ANCESTRO RAIZ en el mapa de clasificadores.  Para clases anidadas,
    // todos los tipos comparten el mismo namespace de paquete Java.
    //
    private Map<String, List<UmlClassifier>> agruparPorPaquete(Collection<UmlClassifier> clasificadores) {
        Map<String, List<UmlClassifier>> agrupados = new LinkedHashMap<>();

        for (UmlClassifier classifier : clasificadores) {
            String packageName = resolverPaquete(classifier.qualifiedName());
            agrupados.computeIfAbsent(packageName, k -> new ArrayList<>()).add(classifier);
        }

        return agrupados;
    }

    /**
     * Extrae el segmento de paquete (minúsculas) de un qualifiedName.
     * El qualifiedName usa "::" como separador.
     * Ejemplo:
     *   "com::unimag::NestedProject::Module"  →  "com::unimag"
     *   "com::unimag::Connectable"             →  "com::unimag"
     *   "MyClass"                              →  "sin_paquete"
     */
    private String resolverPaquete(String qualifiedName) {
        String[] parts = qualifiedName.split("::");
        List<String> packageParts = new ArrayList<>();
        for (String part : parts) {
            if (Character.isLowerCase(part.charAt(0))) {
                packageParts.add(part);
            } else {
                break;  // primer segmento con mayúscula = inicio del nombre de tipo
            }
        }
        if (packageParts.isEmpty()) return "sin_paquete";
        return String.join("::", packageParts);
    }

    private String obtenerNombreSimple(String fullName) {
        int lastSep = fullName.lastIndexOf("::");
        if (lastSep == -1) lastSep = fullName.lastIndexOf('.');
        return (lastSep != -1)
                ? fullName.substring(lastSep + (fullName.contains("::") ? 2 : 1))
                : fullName;
    }

    private String sanitizarAlias(String qualifiedName) {
        return qualifiedName.replace(".", "_").replace(":", "_");
    }

    // ── Enrutamiento y asignación de puntos de conexión (cardinales) ─────────
    //
    // PlantUML permite indicar la dirección de llegada de los conectores
    // mediante palabras clave cardinales:
    //   - up    : la flecha sube hacia el destino (llega al borde INFERIOR)
    //   - right : la flecha va a la derecha hacia el destino (llega al borde IZQUIERDO)
    //   - down  : la flecha baja hacia el destino (llega al borde SUPERIOR)
    //   - left  : la flecha va a la izquierda hacia el destino (llega al borde DERECHO)
    //
    // Cuando una clase recibe múltiples conexiones (por ejemplo, anidamiento +
    // herencias + realizaciones), asignar cíclicamente estas direcciones y niveles
    // de longitud (-- / ..) garantiza que cada relación llegue a un punto/lado
    // diferente del recuadro, evitando la sobrescritura de puntas de flecha y símbolos (+--).
    //
    private static final String[] DIRECCIONES = {"up", "right", "down", "left"};

    private Map<UmlRelationship, String> asignarConectoresPorDestino(List<UmlRelationship> relaciones) {
        Map<UmlRelationship, String> conectores = new IdentityHashMap<>();

        // 1. Agrupar relaciones exportables por su clasificador receptor (destino gráfico)
        Map<String, List<UmlRelationship>> relacionesPorReceptor = new LinkedHashMap<>();

        for (UmlRelationship rel : relaciones) {
            if (rel instanceof UmlAssociation) continue; // asociaciones no se dibujan como flechas independientes
            String receptorKey = obtenerReceptorKey(rel);
            if (receptorKey != null && !receptorKey.isEmpty()) {
                relacionesPorReceptor.computeIfAbsent(receptorKey, k -> new ArrayList<>()).add(rel);
            }
        }

        // 2. Asignar los conectores adecuados
        for (Map.Entry<String, List<UmlRelationship>> entry : relacionesPorReceptor.entrySet()) {
            List<UmlRelationship> lista = entry.getValue();

            if (lista.size() <= 1) {
                // Caso simple: la clase sólo recibe una conexión
                UmlRelationship rel = lista.get(0);
                conectores.put(rel, obtenerConectorSimple(rel));
            } else {
                // Caso múltiple: la clase recibe varias conexiones.
                // Ordenamos por prioridad para favorecer direcciones naturales:
                // herencia ("up") -> realización -> dependencia -> nesting ("right" / lateral)
                lista.sort(Comparator.comparingInt(this::obtenerPrioridadTipoRelacion));

                for (int slot = 0; slot < lista.size(); slot++) {
                    UmlRelationship rel = lista.get(slot);
                    conectores.put(rel, construirConectorConDireccion(rel, slot));
                }
            }
        }

        return conectores;
    }

    private String obtenerReceptorKey(UmlRelationship rel) {
        if (rel instanceof UmlNesting nesting) {
            return nesting.outerClassifier() != null ? nesting.outerClassifier().qualifiedName() : null;
        }
        return rel.target() != null ? rel.target().qualifiedName() : null;
    }

    private int obtenerPrioridadTipoRelacion(UmlRelationship rel) {
        if (rel instanceof UmlGeneralization) return 1;
        if (rel instanceof UmlRealization)    return 2;
        if (rel instanceof UmlDependency)     return 3;
        if (rel instanceof UmlNesting)        return 4;
        return 5;
    }

    private String obtenerConectorSimple(UmlRelationship rel) {
        if (rel instanceof UmlNesting)        return "+--";
        if (rel instanceof UmlGeneralization) return "-up-|>";
        if (rel instanceof UmlRealization)    return ".up.|>";
        if (rel instanceof UmlDependency)     return ".up.|>";
        return null;
    }

    private String construirConectorConDireccion(UmlRelationship rel, int slot) {
        String dir = DIRECCIONES[slot % DIRECCIONES.length];
        int extraDashes = slot / DIRECCIONES.length;
        String dashes = "-".repeat(extraDashes + 1);
        String dots = ".".repeat(extraDashes + 1);

        if (rel instanceof UmlNesting) {
            return "+" + dashes + dir + "-";
        }
        if (rel instanceof UmlGeneralization) {
            return dashes + dir + "-|>";
        }
        if (rel instanceof UmlRealization) {
            return dots + dir + ".|>";
        }
        if (rel instanceof UmlDependency) {
            return dots + dir + ".|>";
        }
        return null;
    }
}