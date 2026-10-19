package generador.infrastructure.export;

import generador.core.domain.classifier.UmlClass;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.classifier.UmlEnumeration;
import generador.core.domain.classifier.UmlInterface;
import generador.core.domain.element.UmlNamespace;
import generador.core.domain.model.UmlModel;
import generador.core.domain.relationship.*;
import generador.core.domain.spec.AggregationKind;
import generador.core.domain.spec.UmlVisibility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConnectionRelationshipFilterTest {

    public static void main(String[] args) throws IOException {
        testConnectionRelationships();
        testNestedInterfaceRelationshipOmitted();
        System.out.println("TEST DE RELACION MAS FUERTE EN CONEXION PASO SATISFACTORIAMENTE.");
    }

    private static void testConnectionRelationships() throws IOException {
        UmlNamespace ns = new UmlNamespace(List.of("com", "ejemplo"));

        UmlClass abstractConn = new UmlClass(
                "AbstractConnection", ns, UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), Optional.empty(), Optional.empty()
        );

        UmlInterface connectable = new UmlInterface(
                "Connectable", ns, UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(),
                Optional.empty(), Optional.empty()
        );

        UmlClass connection = new UmlClass(
                "Connection", ns, UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), Optional.empty(), Optional.empty()
        );

        UmlNamespace nestedNs = ns.child("Connection");
        UmlEnumeration connectionStatus = new UmlEnumeration(
                "ConnectionStatus", nestedNs, UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), List.of(),
                Optional.empty(), Optional.empty()
        );

        Map<String, UmlClassifier> classifiers = new LinkedHashMap<>();
        classifiers.put(abstractConn.qualifiedName(), abstractConn);
        classifiers.put(connectable.qualifiedName(), connectable);
        classifiers.put(connection.qualifiedName(), connection);
        classifiers.put(connectionStatus.qualifiedName(), connectionStatus);

        // Relaciones:
        // 1. Connection extends AbstractConnection (Generalization)
        // 2. Connection implements Connectable (Realization)
        // 3. Connection contiene ConnectionStatus (Nesting)
        // 4. Connection tiene atributo de tipo ConnectionStatus (Association)
        Set<UmlRelationship> relationships = new LinkedHashSet<>();
        relationships.add(new UmlGeneralization(connection, abstractConn, Optional.empty(), Optional.empty()));
        relationships.add(new UmlRealization(connection, connectable, Optional.empty(), Optional.empty()));
        relationships.add(new UmlNesting(connection, connectionStatus, Optional.empty(), Optional.empty()));
        relationships.add(new UmlAssociation(connection, "", connectionStatus, "", AggregationKind.NONE, Optional.empty(), Optional.empty()));

        UmlModel model = new UmlModel(classifiers, relationships);

        // Verificación 1: El modelo contiene las 4 relaciones (incluidas ambas entre Connection y ConnectionStatus)
        if (model.relationships().size() != 4) {
            throw new AssertionError("El modelo debería conservar todas las relaciones (4), pero tiene: " + model.relationships().size());
        }

        // Verificación 2: El filtro gráfico conserva exactamente 2 relaciones:
        // - El nesting sobre ConnectionStatus (descarta la asociación)
        // - La realización sobre Connectable (descarta la herencia AbstractConnection por ser más débil)
        List<UmlRelationship> filtered = UmlRelationshipFilter.filterStrongest(model.relationships());
        if (filtered.size() != 2) {
            throw new AssertionError("Las relaciones filtradas gráficamente deberían ser 2, pero son: " + filtered.size());
        }

        boolean hasNesting = filtered.stream().anyMatch(r -> r instanceof UmlNesting);
        boolean hasRealization = filtered.stream().anyMatch(r -> r instanceof UmlRealization);
        boolean hasGeneralization = filtered.stream().anyMatch(r -> r instanceof UmlGeneralization);
        boolean hasAssociation = filtered.stream().anyMatch(r -> r instanceof UmlAssociation);

        if (!hasNesting) {
            throw new AssertionError("La relación más fuerte (Nesting) debió conservarse.");
        }
        if (!hasRealization) {
            throw new AssertionError("La relación de interfaz (Realization) debió conservarse.");
        }
        if (hasGeneralization) {
            throw new AssertionError("La herencia (Generalization) debió filtrarse frente a la realización (Realization).");
        }
        if (hasAssociation) {
            throw new AssertionError("La relación más débil (Association) debió filtrarse frente al Nesting.");
        }

        // Verificación 3: Exportación a PlantUML
        Path tempPuml = Files.createTempFile("connection_puml", ".puml");
        new UmlModelPlantUmlExporter().export(model, tempPuml);
        String pumlContent = Files.readString(tempPuml);
        Files.deleteIfExists(tempPuml);

        if (!pumlContent.contains("+--")) {
            throw new AssertionError("El PlantUML debe contener el conector de anidamiento (+--).");
        }
        if (!pumlContent.contains(".up.|>")) {
            throw new AssertionError("El PlantUML debe contener la flecha punteada de realización (.up.|>).");
        }
        if (pumlContent.contains("com__ejemplo__Connection -up-|> com__ejemplo__AbstractConnection")) {
            throw new AssertionError("El PlantUML NO debe contener la flecha sólida de herencia hacia AbstractConnection.");
        }

        // Verificación 4: Exportación a Markdown (conserva ambas relaciones)
        Path tempMd = Files.createTempFile("connection_md", ".md");
        new UmlModelMarkdownExporter().export(model, new generador.application.project.AnalysisConfiguration(
                generador.application.project.JavaLanguageVersion.JAVA_21, List.of(), List.of()
        ), tempMd);
        String mdContent = Files.readString(tempMd);
        Files.deleteIfExists(tempMd);

        if (!mdContent.contains("UmlNesting") || !mdContent.contains("UmlAssociation")) {
            throw new AssertionError("El Markdown debe documentar tanto UmlNesting como UmlAssociation.");
        }
    }

    private static void testNestedInterfaceRelationshipOmitted() throws IOException {
        UmlNamespace ns = new UmlNamespace(List.of("com", "ejemplo"));

        UmlClass classA = new UmlClass(
                "OuterA", ns, UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), Optional.empty(), Optional.empty()
        );

        UmlNamespace nestedNs = ns.child("OuterA");
        UmlInterface interfaceC = new UmlInterface(
                "InterfaceC", nestedNs, UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), Optional.empty(), Optional.empty()
        );

        UmlClass classB = new UmlClass(
                "ClassB", nestedNs, UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), Optional.empty(), Optional.empty()
        );

        Map<String, UmlClassifier> classifiers = new LinkedHashMap<>();
        classifiers.put(classA.qualifiedName(), classA);
        classifiers.put(interfaceC.qualifiedName(), interfaceC);
        classifiers.put(classB.qualifiedName(), classB);

        // Relaciones:
        // 1. OuterA anida InterfaceC (UmlNesting)
        // 2. OuterA anida ClassB (UmlNesting)
        // 3. ClassB implementa InterfaceC (UmlRealization)
        Set<UmlRelationship> relationships = new LinkedHashSet<>();
        relationships.add(new UmlNesting(classA, interfaceC, Optional.empty(), Optional.empty()));
        relationships.add(new UmlNesting(classA, classB, Optional.empty(), Optional.empty()));
        relationships.add(new UmlRealization(classB, interfaceC, Optional.empty(), Optional.empty()));

        UmlModel model = new UmlModel(classifiers, relationships);

        List<UmlRelationship> filtered = UmlRelationshipFilter.filterStrongest(model.relationships());

        // Debe conservar ambos nestings y descartar la relación entre ClassB e InterfaceC
        if (filtered.size() != 2) {
            throw new AssertionError("Se esperaban exactamente 2 relaciones (ambos nestings), pero fue: " + filtered.size());
        }

        boolean hasRealization = filtered.stream().anyMatch(r -> r instanceof UmlRealization);
        if (hasRealization) {
            throw new AssertionError("La realización entre la clase anidada B y la interfaz anidada C no debe dibujarse.");
        }

        // Exportar a PlantUML y verificar
        Path tempPuml = Files.createTempFile("nested_iface_puml", ".puml");
        new UmlModelPlantUmlExporter().export(model, tempPuml);
        String pumlContent = Files.readString(tempPuml);
        Files.deleteIfExists(tempPuml);

        if (pumlContent.contains("InterfaceC") && pumlContent.contains(".up.|>") && pumlContent.contains("ClassB")) {
            throw new AssertionError("PlantUML no debe dibujar la relación entre ClassB e InterfaceC.");
        }
    }
}
