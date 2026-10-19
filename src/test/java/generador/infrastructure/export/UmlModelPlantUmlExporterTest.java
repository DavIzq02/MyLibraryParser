package generador.infrastructure.export;

import generador.core.domain.classifier.UmlClass;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.element.UmlNamespace;
import generador.core.domain.model.UmlModel;
import generador.core.domain.relationship.*;
import generador.core.domain.spec.UmlVisibility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class UmlModelPlantUmlExporterTest {

    public static void main(String[] args) throws IOException {
        testSingleConnection();
        testMultipleConnectionsWithNestingAndInheritance();
        testManyInheritances();
        System.out.println("TODOS LOS TESTS DE PLANTUML EXPORTER PASARON SATISFACTORIAMENTE.");
    }

    private static UmlClass createClass(String pkg, String name) {
        UmlNamespace ns = new UmlNamespace(Arrays.asList(pkg.split("\\.")));
        return new UmlClass(
                name,
                ns,
                UmlVisibility.PUBLIC,
                Collections.emptyList(),
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Optional.empty());
    }

    private static void testSingleConnection() throws IOException {
        UmlClass base = createClass("com.example", "Base");
        UmlClass sub = createClass("com.example", "Sub");

        Map<String, UmlClassifier> classifiers = new LinkedHashMap<>();
        classifiers.put(base.qualifiedName(), base);
        classifiers.put(sub.qualifiedName(), sub);

        Set<UmlRelationship> relationships = new LinkedHashSet<>();
        relationships.add(new UmlGeneralization(sub, base, Optional.empty(), Optional.empty()));

        UmlModel model = new UmlModel(classifiers, relationships);

        Path tempFile = Files.createTempFile("puml_test_single", ".puml");
        UmlModelPlantUmlExporter exporter = new UmlModelPlantUmlExporter();
        exporter.export(model, tempFile);

        String content = Files.readString(tempFile);
        Files.deleteIfExists(tempFile);

        // Al recibir sólo una conexión, debe usar la flecha simple "-up-|>"
        if (!content.contains("com__example__Sub -up-|> com__example__Base")) {
            throw new AssertionError("Fallo en testSingleConnection: esperaba -up-|> pero fue:\n" + content);
        }
    }

    private static void testMultipleConnectionsWithNestingAndInheritance() throws IOException {
        UmlClass outer = createClass("com.example", "Outer");
        UmlClass inner = createClass("com.example", "Inner");
        UmlClass sub = createClass("com.example", "Sub");

        Map<String, UmlClassifier> classifiers = new LinkedHashMap<>();
        classifiers.put(outer.qualifiedName(), outer);
        classifiers.put(inner.qualifiedName(), inner);
        classifiers.put(sub.qualifiedName(), sub);

        Set<UmlRelationship> relationships = new LinkedHashSet<>();
        relationships.add(new UmlNesting(outer, inner, Optional.empty(), Optional.empty()));
        relationships.add(new UmlGeneralization(sub, outer, Optional.empty(), Optional.empty()));

        UmlModel model = new UmlModel(classifiers, relationships);

        Path tempFile = Files.createTempFile("puml_test_multi", ".puml");
        UmlModelPlantUmlExporter exporter = new UmlModelPlantUmlExporter();
        exporter.export(model, tempFile);

        String content = Files.readString(tempFile);
        Files.deleteIfExists(tempFile);

        // Outer recibe 2 conexiones: 1 herencia y 1 nesting.
        // Deben tener direcciones distintas para no sobrescribirse en el mismo punto
        // gráfico.
        // Herencia (prioridad 1) obtiene "-up-|>"
        // Nesting obtiene "com__example__Inner +-- com__example__Outer"
        if (!content.contains("com__example__Sub -up-|> com__example__Outer")) {
            throw new AssertionError("Fallo: Herencia no encontrada con -up-|>. Contenido:\n" + content);
        }
        if (!content.contains("com__example__Inner +-- com__example__Outer")) {
            throw new AssertionError("Fallo: Nesting no encontrado con +--. Contenido:\n" + content);
        }
    }

    private static void testManyInheritances() throws IOException {
        UmlClass base = createClass("com.example", "BaseInterface");
        UmlClass s1 = createClass("com.example", "Impl1");
        UmlClass s2 = createClass("com.example", "Impl2");
        UmlClass s3 = createClass("com.example", "Impl3");
        UmlClass s4 = createClass("com.example", "Impl4");
        UmlClass s5 = createClass("com.example", "Impl5");

        Map<String, UmlClassifier> classifiers = new LinkedHashMap<>();
        classifiers.put(base.qualifiedName(), base);
        classifiers.put(s1.qualifiedName(), s1);
        classifiers.put(s2.qualifiedName(), s2);
        classifiers.put(s3.qualifiedName(), s3);
        classifiers.put(s4.qualifiedName(), s4);
        classifiers.put(s5.qualifiedName(), s5);

        Set<UmlRelationship> relationships = new LinkedHashSet<>();
        relationships.add(new UmlGeneralization(s1, base, Optional.empty(), Optional.empty()));
        relationships.add(new UmlGeneralization(s2, base, Optional.empty(), Optional.empty()));
        relationships.add(new UmlGeneralization(s3, base, Optional.empty(), Optional.empty()));
        relationships.add(new UmlGeneralization(s4, base, Optional.empty(), Optional.empty()));
        relationships.add(new UmlGeneralization(s5, base, Optional.empty(), Optional.empty()));

        UmlModel model = new UmlModel(classifiers, relationships);

        Path tempFile = Files.createTempFile("puml_test_many", ".puml");
        UmlModelPlantUmlExporter exporter = new UmlModelPlantUmlExporter();
        exporter.export(model, tempFile);

        String content = Files.readString(tempFile);
        Files.deleteIfExists(tempFile);

        // Las 4 direcciones deben estar presentes: up, right, down, left
        // Y la 5ta debe usar --up-|> con doble guión para espaciar el nivel
        if (!content.contains("-up-|>"))
            throw new AssertionError("Falta -up-|>");
        if (!content.contains("-right-|>"))
            throw new AssertionError("Falta -right-|>");
        if (!content.contains("-down-|>"))
            throw new AssertionError("Falta -down-|>");
        if (!content.contains("-left-|>"))
            throw new AssertionError("Falta -left-|>");
        if (!content.contains("--up-|>"))
            throw new AssertionError("Falta --up-|> para la 5ta conexión");
    }
}
