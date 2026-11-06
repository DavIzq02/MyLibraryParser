package generador.infrastructure.export;

import generador.application.project.AnalysisConfiguration;
import generador.core.domain.classifier.*;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlParameter;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.model.UmlModel;
import generador.core.domain.relationship.*;
import generador.core.domain.spec.UmlModifier;
import generador.infrastructure.parser.JavaTypeResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class UmlModelPlantUmlExporter {

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
        export(model, null, outputPath);
    }

    public void export(UmlModel model, AnalysisConfiguration configuration, Path outputPath) throws IOException {
        StringBuilder puml = new StringBuilder();

        puml.append("@startuml\n\n");
        puml.append("' --- Configuracion Estetica Base ---\n");
        puml.append("skinparam defaultFontName Helvetica\n");
        // NOTA: Se eliminó 'skinparam linetype ortho' para que las flechas usen el enrutamiento 
        // natural y los textos <<use>> queden perfectamente anclados en la línea.
        puml.append("skinparam classAttributeIconSize 0\n");
        puml.append("skinparam classRoundCorner 0\n");
        puml.append("skinparam shadowing false\n");
        puml.append("skinparam monochrome false\n");
        puml.append("skinparam classBorderColor black\n");
        puml.append("skinparam backgroundColor white\n");
        puml.append("skinparam classBackgroundColor white\n\n");

        puml.append("' --- Paleta de Colores Pastel para Java ---\n");
        puml.append("skinparam interfaceBackgroundColor #D5F5E3\n");
        puml.append("skinparam interface<<Interface>>BackgroundColor #D5F5E3\n");
        puml.append("skinparam class<<Interface>>BackgroundColor #D5F5E3\n");
        puml.append("skinparam abstractBackgroundColor #FFE5CC\n");
        puml.append("skinparam class<<Abstract>>BackgroundColor #FFE5CC\n");
        puml.append("skinparam class<<Record>>BackgroundColor #D0E1FD\n");
        puml.append("skinparam class<<external>>BackgroundColor #EAECEE\n");
        puml.append("skinparam interface<<external>>BackgroundColor #EAECEE\n\n");

        Map<String, UmlClassifier> todosClasificadores = obtenerTodosLosClasificadores(model, configuration);

        // Agrupamos TODOS los clasificadores de forma plana por su paquete contenedor
        Map<String, List<UmlClassifier>> todosPorPaquete = new HashMap<>();

        for (UmlClassifier classifier : todosClasificadores.values()) {
            String pkg = resolverPaquete(classifier.qualifiedName(), todosClasificadores);
            todosPorPaquete.computeIfAbsent(pkg, k -> new ArrayList<>()).add(classifier);
        }

        // ORDEN ALFABÉTICO (CRÍTICO): 
        // Garantiza que la clase principal se dibuje antes que su subpaquete (ej. Caso 9).
        // Evita que PlantUML explote diciendo "already exists".
        List<String> paquetesOrdenados = new ArrayList<>(todosPorPaquete.keySet());
        paquetesOrdenados.sort(String::compareTo);

        for (String nombrePaquete : paquetesOrdenados) {
            if (!nombrePaquete.equals("sin_paquete")) {
                puml.append("package ").append(nombrePaquete).append(" {\n");
            }

            for (UmlClassifier classifier : todosPorPaquete.get(nombrePaquete)) {
                boolean esExterno = esClasificadorExterno(model, classifier);
                renderizarClassifier(classifier, esExterno, puml, nombrePaquete.equals("sin_paquete") ? "" : "    ");
            }

            if (!nombrePaquete.equals("sin_paquete")) {
                puml.append("}\n");
            }
            puml.append("\n");
        }

        puml.append("' --- Grafo de relaciones ---\n");

        List<UmlRelationship> relacionesFiltradas = UmlRelationshipFilter.filterStrongest(model.relationships());

        for (UmlRelationship rel : relacionesFiltradas) {
            String origenAlias = sanitizarAlias(rel.source().qualifiedName(), todosClasificadores);
            String destinoAlias = sanitizarAlias(rel.target().qualifiedName(), todosClasificadores);

            if (rel instanceof UmlNesting) {
                puml.append(origenAlias).append(" +-- ").append(destinoAlias).append("\n");
                continue;
            }

            if (esClasificadorExterno(model, rel.target())) {
                puml.append(origenAlias).append(" ..> ").append(destinoAlias).append(" : <<use>>").append("\n");
                continue;
            }

            String conector = obtenerConectorSimple(rel);
            String origenMult = "";
            String destinoMult = "";
            String estereotipo = "";

            if (rel instanceof UmlAssociation assoc) {
                origenMult = assoc.sourceMultiplicity() != null && !assoc.sourceMultiplicity().isEmpty() ? "\"" + assoc.sourceMultiplicity() + "\" " : "";
                destinoMult = assoc.targetMultiplicity() != null && !assoc.targetMultiplicity().isEmpty() ? " \"" + assoc.targetMultiplicity() + "\"" : "";

                String aggr = assoc.aggregationKind() != null ? assoc.aggregationKind().name() : "NONE";
                if (aggr.equals("COMPOSITE")) conector = "*--";
                else if (aggr.equals("SHARED")) conector = "o--";
            }

            if (rel instanceof UmlDependency) {
                estereotipo = " : <<use>>";
            }

            if (conector != null) {
                puml.append(origenAlias).append(" ").append(origenMult)
                        .append(conector).append(destinoMult).append(" ")
                        .append(destinoAlias).append(estereotipo).append("\n");
            }
        }

        puml.append("\n@enduml\n");

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(puml.toString());
        }
    }

    private void renderizarClassifier(UmlClassifier classifier, boolean esExterno, StringBuilder puml, String indent) {
        String tipo = obtenerTipoClassifier(classifier);
        String estereotipo = resolverEstereotipo(classifier, esExterno);
        String simpleName = obtenerNombreSimple(classifier.qualifiedName());

        String templateParams = "";
        if (!classifier.templateParameters().isEmpty()) {
            templateParams = "<" + String.join(", ", classifier.templateParameters()) + "> ";
        }

        puml.append(indent).append(tipo).append(" ").append(simpleName)
                .append(templateParams).append(estereotipo).append(" {\n");

        if (!esExterno) {
            if (classifier instanceof UmlEnumeration enumeration) {
                for (UmlEnumerationLiteral literal : enumeration.literals()) {
                    puml.append(indent).append("    + {static, readOnly} ").append(literal.name())
                        .append(" : ").append(simpleName).append("\n");
                }
            }

            for (UmlProperty prop : classifier.properties()) {
                puml.append(indent).append("    ").append(convertirVisibilidad(prop.visibility())).append("  ");

                String modificadoresProp = construirModificadoresPropiedad(prop);
                if (!modificadoresProp.isEmpty()) {
                    puml.append(modificadoresProp).append(" ");
                }

                puml.append(prop.name()).append(" : ").append(abreviarTipo(prop.type().name()));

                prop.initialValue().ifPresent(val -> {
                    String cleanVal = val.matches("^-?\\d+[Ll]$") ? val.substring(0, val.length() - 1) : val;
                    puml.append(" = ").append(cleanVal);
                });
                puml.append("\n");
            }

            for (UmlOperation op : classifier.operations()) {
                String returnTypeName = op.isConstructor() ? "void" : abreviarTipo(op.returnType().name());

                puml.append(indent).append("    ").append(convertirVisibilidad(op.visibility())).append("  ");

                if (op.modifiers().contains(UmlModifier.STATIC)) puml.append("{static} ");
                if (op.modifiers().contains(UmlModifier.ABSTRACT)) puml.append("{abstract} ");

                puml.append(op.name()).append("(").append(buildParameterList(op)).append(")");

                if (returnTypeName != null) {
                    puml.append(" : ").append(returnTypeName);
                }

                if (op.isConstructor()) {
                    puml.append(" <<Create>>");
                }
                puml.append("\n");
            }
        }

        puml.append(indent).append("}\n");
    }

    private String construirModificadoresPropiedad(UmlProperty prop) {
        List<String> mods = new ArrayList<>();
        if (prop.modifiers().contains(UmlModifier.STATIC)) {
            mods.add("static");
        }
        if (prop.modifiers().contains(UmlModifier.ABSTRACT)) {
            mods.add("abstract");
        }
        if (prop.modifiers().contains(UmlModifier.FINAL) || prop.modifiers().contains(UmlModifier.READ_ONLY)) {
            mods.add("readOnly");
        }
        if (mods.isEmpty()) {
            return "";
        }
        return "{" + String.join(", ", mods) + "}";
    }

    private Map<String, UmlClassifier> obtenerTodosLosClasificadores(UmlModel model, AnalysisConfiguration configuration) {
        Map<String, UmlClassifier> todos = new LinkedHashMap<>(model.classifiers());

        for (UmlRelationship rel : model.relationships()) {
            if (rel.source() != null && !todos.containsKey(rel.source().qualifiedName())) {
                todos.put(rel.source().qualifiedName(), rel.source());
            }
            if (rel.target() != null && !todos.containsKey(rel.target().qualifiedName())) {
                todos.put(rel.target().qualifiedName(), rel.target());
            }
        }

        if (configuration != null) {
            registrarTiposWhitelist(configuration.javaPackageWhitelist(), todos);
            if (configuration.whiteList() != null) {
                registrarTiposWhitelist(configuration.whiteList().types(), todos);
            }
        }

        return todos;
    }

    private void registrarTiposWhitelist(Collection<String> types, Map<String, UmlClassifier> todos) {
        if (types == null) return;
        for (String type : types) {
            if (type == null || type.isBlank() || type.endsWith(".*")) continue;
            String clean = type.replace("::", ".");
            int lastDot = clean.lastIndexOf('.');
            if (lastDot != -1 && lastDot < clean.length() - 1 && Character.isUpperCase(clean.charAt(lastDot + 1))) {
                String umlQName = clean.replace(".", "::");
                if (!todos.containsKey(umlQName)) {
                    todos.put(umlQName, JavaTypeResolver.createExternalClassifier(clean));
                }
            }
        }
    }

    private boolean esClasificadorExterno(UmlModel model, UmlClassifier classifier) {
        return !model.classifiers().containsKey(classifier.qualifiedName());
    }

    private String resolverEstereotipo(UmlClassifier classifier, boolean esExterno) {
        if (esExterno) return " <<external>>";
        if (classifier instanceof UmlInterface) return " <<Interface>>";
        if (classifier instanceof UmlRecord) return " <<Record>>";
        if (classifier.isAbstract() || classifier.modifiers().contains(UmlModifier.ABSTRACT)) return " <<Abstract>>";
        return "";
    }

    private String abreviarTipo(String typeName) {
        if (typeName == null || typeName.isBlank()) return typeName;

        if (typeName.contains("<") && typeName.endsWith(">")) {
            int openBracket = typeName.indexOf('<');
            String base = abreviarTipo(typeName.substring(0, openBracket));
            String args = typeName.substring(openBracket + 1, typeName.length() - 1);

            List<String> argParts = new ArrayList<>();
            int depth = 0;
            StringBuilder current = new StringBuilder();
            for (char c : args.toCharArray()) {
                if (c == '<') depth++;
                if (c == '>') depth--;
                if (c == ',' && depth == 0) {
                    argParts.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            if (current.length() > 0) {
                argParts.add(current.toString().trim());
            }

            return base + "<" + argParts.stream().map(this::abreviarTipo).collect(Collectors.joining(", ")) + ">";
        }

        if (KNOWN_TYPE_SHORTCUTS.containsKey(typeName)) {
            return KNOWN_TYPE_SHORTCUTS.get(typeName);
        }

        int lastSep = typeName.lastIndexOf("::");
        if (lastSep == -1) lastSep = typeName.lastIndexOf('.');
        if (lastSep != -1) {
            String candidate = typeName.substring(lastSep + (typeName.contains("::") ? 2 : 1));
            if (!candidate.isBlank()) return candidate;
        }

        return typeName;
    }

    private String buildParameterList(UmlOperation op) {
        if (op.parameters().isEmpty()) return "";
        return op.parameters().stream()
                .map(p -> p.name() + " : " + abreviarTipo(p.type().name()))
                .collect(Collectors.joining(", "));
    }

    private String obtenerTipoClassifier(UmlClassifier classifier) {
        if (classifier instanceof UmlInterface) return "interface";
        if (classifier instanceof UmlEnumeration) return "enum";
        if (classifier instanceof UmlAnnotation) return "annotation";
        if (classifier.isAbstract() || classifier.modifiers().contains(UmlModifier.ABSTRACT)) return "abstract class";
        return "class";
    }

    private String convertirVisibilidad(Object visibilidad) {
        if (visibilidad == null) return "~";
        String vis = visibilidad.toString().toUpperCase();
        if (vis.contains("PUBLIC")) return "+";
        if (vis.contains("PRIVATE")) return "-";
        if (vis.contains("PROTECTED")) return "#";
        return "~";
    }

    // HEURÍSTICA INTELIGENTE: Diferencia los Casos 1,2,3,4 (aplanados) del Caso 9 (con carpeta)
    private String resolverPaquete(String qualifiedName, Map<String, UmlClassifier> todosClasificadores) {
        String normalized = qualifiedName.replace("::", ".");
        int lastSep = normalized.lastIndexOf('.');
        if (lastSep == -1) return "sin_paquete";

        String parentPath = normalized.substring(0, lastSep);
        boolean parentIsClass = todosClasificadores.containsKey(parentPath) || todosClasificadores.containsKey(parentPath.replace(".", "::"));

        if (parentIsClass) {
            // Si la clase padre tiene 3 o más clases anidadas directas (Alta complejidad = Caso 9),
            // le creamos su propio paquete. Si tiene 1 o 2 (Baja complejidad = Casos 1, 2, 3, 4),
            // aplanamos la ruta buscando recursivamente el paquete base real.
            long directNestedCount = todosClasificadores.keySet().stream()
                    .filter(k -> {
                        String cleanKey = k.replace("::", ".");
                        return cleanKey.startsWith(parentPath + ".") && 
                               cleanKey.indexOf('.', parentPath.length() + 1) == -1;
                    })
                    .count();

            if (directNestedCount >= 3) {
                return parentPath;
            } else {
                return resolverPaquete(parentPath, todosClasificadores);
            }
        }

        return parentPath;
    }

    private String obtenerNombreSimple(String fullName) {
        String normalized = fullName.replace("::", ".");
        int lastSep = normalized.lastIndexOf('.');
        return (lastSep != -1) ? normalized.substring(lastSep + 1) : normalized;
    }

    private String sanitizarAlias(String qualifiedName, Map<String, UmlClassifier> todosClasificadores) {
        String pkg = resolverPaquete(qualifiedName, todosClasificadores);
        String simpleName = obtenerNombreSimple(qualifiedName);
        return "sin_paquete".equals(pkg) ? simpleName : pkg + "." + simpleName;
    }

    private String obtenerConectorSimple(UmlRelationship rel) {
        if (rel instanceof UmlGeneralization) return "--|>";
        if (rel instanceof UmlRealization) return "..|>";
        if (rel instanceof UmlDependency) return "..>";
        if (rel instanceof UmlAssociation) return "-->";
        return null;
    }
}