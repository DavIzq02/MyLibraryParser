package main.java.generador.infrastructure.export;

import generador.core.domain.classifier.*;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.model.UmlModel;
import generador.core.domain.relationship.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public final class UmlModelPlantUmlExporter {

    public void export(UmlModel model, Path outputPath) throws IOException {
        StringBuilder puml = new StringBuilder();
        puml.append("@startuml\n");
        puml.append("skinparam classAttributeIconSize 0\n");
        puml.append("skinparam linetype ortho\n\n");

        // 1. AGRUPAR CLASIFICADORES POR PAQUETE
        Map<String, List<UmlClassifier>> clasificadoresPorPaquete = agruparPorPaquete(model.classifiers().values());

        for (Map.Entry<String, List<UmlClassifier>> entry : clasificadoresPorPaquete.entrySet()) {
            String nombrePaquete = entry.getKey();
            List<UmlClassifier> listaClases = entry.getValue();

            puml.append("package \"").append(nombrePaquete).append("\" {\n");

            for (UmlClassifier classifier : listaClases) {
                String tipo = obtenerTipoClassifier(classifier);
                String estereotipo = esRecord(classifier) ? " <<record>>" : "";
                String fullName = classifier.qualifiedName();
                String alias = sanitizarAlias(fullName);
                String simpleName = obtenerNombreSimple(fullName);

                // Imprime: class "CursoDto" as alias <<record>> {
                puml.append("  ").append(tipo).append(" \"").append(simpleName)
                    .append("\" as ").append(alias).append(estereotipo).append(" {\n");

                // Propiedades / Atributos
                for (UmlProperty prop : classifier.properties()) {
                    puml.append("    ").append(convertirVisibilidad(prop.visibility())).append(" ")
                        .append(prop.name()).append(" : ")
                        .append(prop.type().name()).append("\n");
                }

                if (!classifier.properties().isEmpty() && !classifier.operations().isEmpty()) {
                    puml.append("    --\n");
                }

                // Métodos / Operaciones
                for (UmlOperation op : classifier.operations()) {
                    puml.append("    ").append(convertirVisibilidad(op.visibility())).append(" ")
                        .append(op.name()).append("() : ")
                        .append(op.isConstructor() ? "void" : op.returnType().name()).append("\n");
                }

                puml.append("  }\n\n");
            }

            puml.append("}\n\n");
        }

        puml.append("' --- RELACIÓN DE MÁS PESO POR CLASE ORIGEN ---\n");

        // 2. SELECCIONAR ÚNICAMENTE LA RELACIÓN DE MAYOR PESO
        Map<String, UmlRelationship> relacionesDeMayorPeso = seleccionarRelacionDeMayorPesoPorOrigen(model.relationships());

        // 3. DIBUJAR LÍNEAS DE CONEXIÓN USANDO EL ALIAS SANITIZADO
        for (UmlRelationship rel : relacionesDeMayorPeso.values()) {
            String origenAlias = sanitizarAlias(rel.source().qualifiedName());
            String destinoAlias = sanitizarAlias(rel.target().qualifiedName());
            String conector = obtenerConectorPuml(rel);

            puml.append(origenAlias).append(" ")
                .append(conector).append(" ")
                .append(destinoAlias).append("\n");
        }

        puml.append("\n@enduml\n");

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(puml.toString());
        }
    }

    private boolean esRecord(UmlClassifier classifier) {
        return classifier instanceof UmlRecord;
    }

    private String obtenerTipoClassifier(UmlClassifier classifier) {
        if (classifier instanceof UmlInterface) return "interface";
        if (classifier instanceof UmlEnumeration) return "enum";
        
        // Si es una clase abstracta
        if (classifier.modifiers().toString().toLowerCase().contains("abstract")) {
            return "abstract class";
        }

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
        Map<String, List<UmlClassifier>> agrupados = new HashMap<>();

        for (UmlClassifier classifier : clasificadores) {
            String fullName = classifier.qualifiedName();
            
            int lastSep = fullName.lastIndexOf("::");
            if (lastSep == -1) {
                lastSep = fullName.lastIndexOf('.');
            }
            
            String packageName = (lastSep != -1) ? fullName.substring(0, lastSep) : "sin_paquete";

            agrupados.computeIfAbsent(packageName, k -> new ArrayList<>()).add(classifier);
        }

        return agrupados;
    }

    private String obtenerNombreSimple(String fullName) {
        int lastSep = fullName.lastIndexOf("::");
        if (lastSep == -1) {
            lastSep = fullName.lastIndexOf('.');
        }
        return (lastSep != -1) ? fullName.substring(lastSep + (fullName.contains("::") ? 2 : 1)) : fullName;
    }

    private String sanitizarAlias(String qualifiedName) {
        return qualifiedName.replace(".", "_").replace(":", "_");
    }

    private Map<String, UmlRelationship> seleccionarRelacionDeMayorPesoPorOrigen(Collection<UmlRelationship> relaciones) {
        Map<String, UmlRelationship> mapaRelaciones = new HashMap<>();

        for (UmlRelationship rel : relaciones) {
            String origenId = rel.source().qualifiedName();

            if (!mapaRelaciones.containsKey(origenId)) {
                mapaRelaciones.put(origenId, rel);
            } else {
                UmlRelationship relacionExistente = mapaRelaciones.get(origenId);
                if (calcularPeso(rel) > calcularPeso(relacionExistente)) {
                    mapaRelaciones.put(origenId, rel);
                }
            }
        }
        return mapaRelaciones;
    }

    private int calcularPeso(UmlRelationship rel) {
        if (rel instanceof UmlGeneralization) return 4; 
        if (rel instanceof UmlRealization) return 3;    
        if (rel instanceof UmlAssociation) return 2;    
        if (rel instanceof UmlDependency) return 1;     
        return 0;
    }

    private String obtenerConectorPuml(UmlRelationship rel) {
        if (rel instanceof UmlGeneralization) return "--|>";
        if (rel instanceof UmlRealization) return "..|>";
        if (rel instanceof UmlAssociation) return "-->";
        if (rel instanceof UmlDependency) return "..>";
        return "-->";
    }
}