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

        puml.append("@startuml\n\n");
        puml.append("' --- Configuracion Estetica Base ---\n");
        puml.append("skinparam defaultFontName Helvetica\n");
        puml.append("skinparam linetype ortho\n");
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

        // Incluye tanto clasificadores internos como los tipos externos referenciados (Caso 01)
        Map<String, UmlClassifier> todosClasificadores = obtenerTodosLosClasificadores(model);
        Map<String, List<UmlClassifier>> clasificadoresPorPaquete = agruparPorPaquete(todosClasificadores.values());

        for (Map.Entry<String, List<UmlClassifier>> entry : clasificadoresPorPaquete.entrySet()) {
            String nombrePaquete = entry.getKey();

            if (!nombrePaquete.equals("sin_paquete")) {
                puml.append("package ").append(nombrePaquete).append(" {\n");
            }

            for (UmlClassifier classifier : entry.getValue()) {
                boolean esExterno = esClasificadorExterno(model, classifier);
                String tipo = obtenerTipoClassifier(classifier);
                String estereotipo = resolverEstereotipo(classifier, esExterno);
                String simpleName = obtenerNombreSimple(classifier.qualifiedName());

                puml.append("    ").append(tipo).append(" ").append(simpleName)
                        .append(estereotipo).append(" {\n");

                // Para tipos externos (como BigDecimal u OffsetDateTime), se omite el cuerpo
                if (!esExterno) {
                    if (classifier instanceof UmlEnumeration enumeration) {
                        for (UmlEnumerationLiteral literal : enumeration.literals()) {
                            puml.append("    +  ").append(literal.name()).append("\n");
                        }
                    }

                    for (UmlProperty prop : classifier.properties()) {
                        puml.append("    ").append(convertirVisibilidad(prop.visibility())).append("  ");

                        if (prop.modifiers().contains(UmlModifier.STATIC)) puml.append("{static} ");
                        if (prop.modifiers().contains(UmlModifier.ABSTRACT)) puml.append("{abstract} ");
                        if (prop.modifiers().contains(UmlModifier.FINAL) || prop.modifiers().contains(UmlModifier.READ_ONLY)) {
                            puml.append("{readOnly} ");
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

                        puml.append("    ").append(convertirVisibilidad(op.visibility())).append("  ");

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

                puml.append("    }\n");
            }

            if (!nombrePaquete.equals("sin_paquete")) {
                puml.append("}\n");
            }
            puml.append("\n");
        }

        puml.append("' --- Grafo de relaciones ---\n");

        List<UmlRelationship> relacionesFiltradas = UmlRelationshipFilter.filterStrongest(model.relationships());

        for (UmlRelationship rel : relacionesFiltradas) {
            String origenAlias = sanitizarAlias(rel.source().qualifiedName());
            String destinoAlias = sanitizarAlias(rel.target().qualifiedName());

            if (rel instanceof UmlNesting) {
                puml.append(origenAlias).append(" +-- ").append(destinoAlias).append("\n");
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

    private Map<String, UmlClassifier> obtenerTodosLosClasificadores(UmlModel model) {
        Map<String, UmlClassifier> todos = new LinkedHashMap<>(model.classifiers());

        // Registra clasificadores externos presentes en las relaciones para exportar sus bloques <<external>>
        for (UmlRelationship rel : model.relationships()) {
            if (rel.source() != null && !todos.containsKey(rel.source().qualifiedName())) {
                todos.put(rel.source().qualifiedName(), rel.source());
            }
            if (rel.target() != null && !todos.containsKey(rel.target().qualifiedName())) {
                todos.put(rel.target().qualifiedName(), rel.target());
            }
        }
        return todos;
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
            argParts.add(current.toString().trim());

            String abreviatedArgs = argParts.stream().map(this::abreviarTipo).collect(Collectors.joining(", "));
            return base + "<" + abreviatedArgs + ">";
        }

        if (typeName.endsWith("[]")) {
            return abreviarTipo(typeName.substring(0, typeName.length() - 2)) + "[]";
        }

        String shortcut = KNOWN_TYPE_SHORTCUTS.get(typeName);
        if (shortcut != null) return shortcut;

        for (Map.Entry<String, String> entry : KNOWN_TYPE_SHORTCUTS.entrySet()) {
            if (typeName.startsWith(entry.getKey())) {
                return entry.getValue() + typeName.substring(entry.getKey().length());
            }
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

    private Map<String, List<UmlClassifier>> agruparPorPaquete(Collection<UmlClassifier> clasificadores) {
        Map<String, List<UmlClassifier>> agrupados = new LinkedHashMap<>();
        for (UmlClassifier classifier : clasificadores) {
            String packageName = resolverPaquete(classifier.qualifiedName());
            agrupados.computeIfAbsent(packageName, k -> new ArrayList<>()).add(classifier);
        }
        return agrupados;
    }

    private String resolverPaquete(String qualifiedName) {
        String normalized = qualifiedName.replace("::", ".");
        String[] parts = normalized.split("\\.");
        List<String> packageParts = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty() && Character.isLowerCase(part.charAt(0))) {
                packageParts.add(part);
            } else {
                break;
            }
        }
        return packageParts.isEmpty() ? "sin_paquete" : String.join(".", packageParts);
    }

    private String obtenerNombreSimple(String fullName) {
        int lastSep = fullName.lastIndexOf("::");
        if (lastSep == -1) lastSep = fullName.lastIndexOf('.');
        return (lastSep != -1) ? fullName.substring(lastSep + (fullName.contains("::") ? 2 : 1)) : fullName;
    }

    private String sanitizarAlias(String qualifiedName) {
        String pkg = resolverPaquete(qualifiedName);
        String simpleName = obtenerNombreSimple(qualifiedName);
        if ("sin_paquete".equals(pkg)) {
            return simpleName;
        }
        return pkg + "." + simpleName;
    }

    private String obtenerConectorSimple(UmlRelationship rel) {
        if (rel instanceof UmlGeneralization) return "--|>";
        if (rel instanceof UmlRealization) return "..|>";
        if (rel instanceof UmlDependency) return "..>";
        if (rel instanceof UmlAssociation) return "-->";
        return null;
    }
}