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

    // ── Tipos Java conocidos que se abrevian para limpieza visual ────────────
    private static final Map<String, String> KNOWN_TYPE_SHORTCUTS = Map.of(
            "java.lang.String", "String",
            "java.lang.Integer", "Integer",
            "java.lang.Long", "Long",
            "java.lang.Double", "Double",
            "java.lang.Boolean", "Boolean",
            "java.lang.Object", "Object",
            "java.util.List", "List",
            "java.util.Map", "Map",
            "java.util.Set", "Set");

    public void export(UmlModel model, Path outputPath) throws IOException {
        StringBuilder puml = new StringBuilder();
        puml.append("@startuml\n");
        puml.append("skinparam classAttributeIconSize 0\n");
        puml.append("skinparam linetype ortho\n\n");

        // ── 1. AGRUPAR CLASIFICADORES POR PAQUETE ──────────────────────────────
        Map<String, List<UmlClassifier>> clasificadoresPorPaquete = agruparPorPaquete(model.classifiers().values());

        for (Map.Entry<String, List<UmlClassifier>> entry : clasificadoresPorPaquete.entrySet()) {
            String nombrePaquete = entry.getKey();
            List<UmlClassifier> listaClases = entry.getValue();

            puml.append("package ").append(nombrePaquete).append(" {\n");

            for (UmlClassifier classifier : listaClases) {

                String tipo = obtenerTipoClassifier(classifier);
                String estereotipo = esRecord(classifier) ? " <<record>>" : "";
                String fullName = classifier.qualifiedName();
                String alias = sanitizarAlias(fullName);
                String simpleName = obtenerNombreSimple(fullName);
                String color = resolverColorClassifier(classifier);
                String colorSuffix = color.isEmpty() ? "" : " " + color;

                puml.append("  ").append(tipo).append(" \"").append(simpleName)
                        .append("\" as ").append(alias).append(estereotipo).append(colorSuffix).append(" {\n");

                // ── Literales de enumeración ───────────────────────────────────
                // Formato: + LITERAL_NAME : EnumName {static, readOnly}
                if (classifier instanceof UmlEnumeration enumeration) {
                    for (UmlEnumerationLiteral literal : enumeration.literals()) {
                        puml.append("    + ").append(literal.name())
                                .append(" : ").append(simpleName)
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
                            .append(abreviarTipo(prop.type().name()));

                    // Valor inicial (ej: timeout: long = 5000)
                    prop.initialValue().ifPresent(val -> {
                        String cleanVal = val.matches("^-?\\d+[Ll]$") ? val.substring(0, val.length() - 1) : val;
                        puml.append(" = ").append(cleanVal);
                    });

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
                    String returnTypeName = op.isConstructor() ? null : abreviarTipo(op.returnType().name());
                    boolean omitirRetorno = returnTypeName == null || "void".equals(returnTypeName);

                    puml.append("    ")
                            .append(convertirVisibilidad(op.visibility()))
                            .append(" ")
                            .append(op.name())
                            .append("(")
                            .append(buildParameterList(op))
                            .append(")");

                    if (!omitirRetorno) {
                        puml.append(" : ").append(returnTypeName);
                    }

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

        // ── 2. RELACIONES (SOLO LA MAS FUERTE POR PAR) ────────────────────────
        List<UmlRelationship> relacionesFiltradas = UmlRelationshipFilter.filterStrongest(model.relationships());
        Map<UmlRelationship, String> conectoresAsignados = asignarConectoresPorDestino(relacionesFiltradas);

        puml.append("' --- RELACIONES DE ANIDAMIENTO (CLASES INTERNAS) ---\n");

        for (UmlRelationship rel : relacionesFiltradas) {
            if (rel instanceof UmlNesting nesting) {
                String outerAlias = sanitizarAlias(nesting.outerClassifier().qualifiedName());
                String innerAlias = sanitizarAlias(nesting.innerClassifier().qualifiedName());
                // Anidada +-- Contenedora (la clase interna se conecta hacia su clase contenedora)
                puml.append(innerAlias).append(" +-- ").append(outerAlias).append("\n");
            }
        }

        puml.append("\n' --- RELACIONES DE HERENCIA E IMPLEMENTACION ---\n");

        // ── 3. HERENCIA Y REALIZACIÓN ──────────────────────────────────────────
        for (UmlRelationship rel : relacionesFiltradas) {
            if (rel instanceof UmlNesting) continue;

            String origenAlias  = sanitizarAlias(rel.source().qualifiedName());
            String destinoAlias = sanitizarAlias(rel.target().qualifiedName());
            String conector     = conectoresAsignados.getOrDefault(rel, obtenerConectorSimple(rel));

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

    /**
     * Resuelve el código de color de fondo según UML 2.5.1:
     * - abstract: Naranja claro (#FFE0B2)
     * - interface: Verde claro (#C8E6C9)
     */
    private String resolverColorClassifier(UmlClassifier classifier) {
        if (classifier instanceof UmlInterface) {
            return "#C8E6C9"; // Verde claro (UML 2.5.1 para interfaz)
        }
        if (classifier.isAbstract() || classifier.modifiers().contains(UmlModifier.ABSTRACT)) {
            return "#FFE0B2"; // Naranja claro (UML 2.5.1 para clase abstracta)
        }
        return "";
    }

    /**
     * Abrevia tipos Java verbosos (java.lang.String → String).
     * Si el nombre tiene parametros genericos, los abrevia recursivamente.
     */
    private String abreviarTipo(String typeName) {
        if (typeName == null || typeName.isBlank())
            return typeName;

        // Manejo de tipos genéricos: ej. List<com::unimag::NestedProject::Module> -> List<Module>
        if (typeName.contains("<") && typeName.endsWith(">")) {
            int openBracket = typeName.indexOf('<');
            String base = abreviarTipo(typeName.substring(0, openBracket));
            String args = typeName.substring(openBracket + 1, typeName.length() - 1);
            String[] argParts = args.split(",\\s*");
            String abreviatedArgs = Arrays.stream(argParts)
                    .map(this::abreviarTipo)
                    .collect(Collectors.joining(", "));
            return base + "<" + abreviatedArgs + ">";
        }

        // Manejo de arrays: ej. String[]
        if (typeName.endsWith("[]")) {
            return abreviarTipo(typeName.substring(0, typeName.length() - 2)) + "[]";
        }

        String shortcut = KNOWN_TYPE_SHORTCUTS.get(typeName);
        if (shortcut != null)
            return shortcut;

        for (Map.Entry<String, String> entry : KNOWN_TYPE_SHORTCUTS.entrySet()) {
            if (typeName.startsWith(entry.getKey())) {
                return entry.getValue() + typeName.substring(entry.getKey().length());
            }
        }

        // Simplificar qualified names con :: o con . (ej: com::unimag::NestedProject::Module -> Module)
        int lastSep = typeName.lastIndexOf("::");
        if (lastSep == -1)
            lastSep = typeName.lastIndexOf('.');
        if (lastSep != -1) {
            String candidate = typeName.substring(lastSep + (typeName.contains("::") ? 2 : 1));
            if (!candidate.isBlank()) {
                return candidate;
            }
        }

        return typeName;
    }

    /** Construye la lista de parámetros formateada: "name : Type, name2 : Type2" */
    private String buildParameterList(UmlOperation op) {
        if (op.parameters().isEmpty())
            return "";
        return op.parameters().stream()
                .map(p -> p.name() + " : " + abreviarTipo(p.type().name()))
                .collect(Collectors.joining(", "));
    }

    /** Etiquetas UML de una propiedad según sus modificadores. */
    private List<String> buildPropertyConstraints(UmlProperty prop) {
        List<String> c = new ArrayList<>();
        if (prop.isStatic())
            c.add("static");
        // FINAL se mapea a readOnly en UML
        if (prop.isFinal() || prop.isReadOnly())
            c.add("readOnly");
        return c;
    }

    /** Etiquetas UML de una operación según sus modificadores. */
    private List<String> buildOperationConstraints(UmlOperation op) {
        List<String> c = new ArrayList<>();
        if (op.isStatic())
            c.add("static");
        if (op.isAbstract())
            c.add("abstract");
        return c;
    }

    // ── Helpers de clasificadores ─────────────────────────────────────────────

    private boolean esRecord(UmlClassifier classifier) {
        return classifier instanceof UmlRecord;
    }

    private String obtenerTipoClassifier(UmlClassifier classifier) {
        if (classifier instanceof UmlInterface)
            return "interface";
        if (classifier instanceof UmlEnumeration)
            return "enum";
        if (classifier instanceof UmlAnnotation)
            return "annotation";
        // abstract class: verificar explícitamente si es abstracta
        if (classifier.isAbstract() || classifier.modifiers().contains(UmlModifier.ABSTRACT))
            return "abstract class";
        return "class";
    }

    private String convertirVisibilidad(Object visibilidad) {
        if (visibilidad == null)
            return "~";
        String vis = visibilidad.toString().toUpperCase();
        if (vis.contains("PUBLIC"))
            return "+";
        if (vis.contains("PRIVATE"))
            return "-";
        if (vis.contains("PROTECTED"))
            return "#";
        return "~";
    }

    // ── Agrupación por paquete ────────────────────────────────────────────────
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
     * Soporta tanto "::" como "." como separador de paquetes.
     * Ejemplo:
     *   "com::unimag::NestedProject::Module"  →  "com.unimag"
     *   "com::unimag::Connectable"             →  "com.unimag"
     *   "MyClass"                              →  "sin_paquete"
     */
    private String resolverPaquete(String qualifiedName) {
        String normalized = qualifiedName.replace("::", ".");
        String[] parts = normalized.split("\\.");
        List<String> packageParts = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty() && Character.isLowerCase(part.charAt(0))) {
                packageParts.add(part);
            } else {
                break; // primer segmento con mayúscula = inicio del nombre de tipo
            }
        }
        if (packageParts.isEmpty())
            return "sin_paquete";
        return String.join(".", packageParts);
    }

    private String obtenerNombreSimple(String fullName) {
        int lastSep = fullName.lastIndexOf("::");
        if (lastSep == -1)
            lastSep = fullName.lastIndexOf('.');
        return (lastSep != -1)
                ? fullName.substring(lastSep + (fullName.contains("::") ? 2 : 1))
                : fullName;
    }

    private String sanitizarAlias(String qualifiedName) {
        return qualifiedName.replace(".", "_").replace(":", "_");
    }

    // ── Enrutamiento y asignación de conectores ───────────────────────────────
    private static final String[] DIRECCIONES = { "up", "right", "down", "left" };

    private Map<UmlRelationship, String> asignarConectoresPorDestino(List<UmlRelationship> relaciones) {
        Map<UmlRelationship, String> conectores = new IdentityHashMap<>();

        // Agrupar relaciones exportables por su clasificador receptor (destino gráfico)
        Map<String, List<UmlRelationship>> relacionesPorReceptor = new LinkedHashMap<>();

        for (UmlRelationship rel : relaciones) {
            if (rel instanceof UmlNesting)
                continue;
            String receptorKey = obtenerReceptorKey(rel);
            if (receptorKey != null && !receptorKey.isEmpty()) {
                relacionesPorReceptor.computeIfAbsent(receptorKey, k -> new ArrayList<>()).add(rel);
            }
        }

        for (Map.Entry<String, List<UmlRelationship>> entry : relacionesPorReceptor.entrySet()) {
            List<UmlRelationship> lista = entry.getValue();

            if (lista.size() <= 1) {
                UmlRelationship rel = lista.get(0);
                conectores.put(rel, obtenerConectorSimple(rel));
            } else {
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
        if (rel instanceof UmlGeneralization)
            return 1;
        if (rel instanceof UmlRealization)
            return 2;
        if (rel instanceof UmlDependency)
            return 3;
        if (rel instanceof UmlAssociation)
            return 4;
        if (rel instanceof UmlNesting)
            return 5;
        return 6;
    }

    private String obtenerConectorSimple(UmlRelationship rel) {
        if (rel instanceof UmlNesting)
            return "+--";
        if (rel instanceof UmlGeneralization)
            return "-up-|>";
        if (rel instanceof UmlRealization)
            return ".up.|>";
        if (rel instanceof UmlDependency)
            return ".up.|>";
        if (rel instanceof UmlAssociation)
            return "-->";
        return null;
    }

    private String construirConectorConDireccion(UmlRelationship rel, int slot) {
        String dir = DIRECCIONES[slot % DIRECCIONES.length];
        int extraDashes = slot / DIRECCIONES.length;
        String dashes = "-".repeat(extraDashes + 1);
        String dots = ".".repeat(extraDashes + 1);

        if (rel instanceof UmlNesting) {
            return "+--";
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
        if (rel instanceof UmlAssociation) {
            return dashes + dir + ">";
        }
        return null;
    }
}
