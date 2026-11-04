package generador.infrastructure.export;

import generador.application.project.AnalysisConfiguration;
import generador.application.project.JavaLanguageVersion;
import generador.core.domain.classifier.UmlClass;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.element.UmlNamespace;
import generador.core.domain.model.UmlModel;
import generador.core.domain.relationship.*;
import generador.core.domain.spec.AggregationKind;
import generador.core.domain.spec.UmlVisibility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class UmlModelPlantUmlExporterTest {

    public static void main(String[] args) throws IOException {
        testSingleConnection();
        testMultipleConnectionsWithNestingAndInheritance();
        testJavaWhitelistPackageAndRelationships();
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

        if (!content.contains("com.example.Sub --|> com.example.Base")) {
            throw new AssertionError("Fallo en testSingleConnection: esperaba '--|>' pero fue:\n" + content);
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

        if (!content.contains("com.example.Sub --|> com.example.Outer")) {
            throw new AssertionError("Fallo: Herencia no encontrada con --|>. Contenido:\n" + content);
        }
        if (!content.contains("com.example.Outer +-- com.example.Inner")) {
            throw new AssertionError("Fallo: Nesting no encontrado con +--. Contenido:\n" + content);
        }
    }

    private static void testJavaWhitelistPackageAndRelationships() throws IOException {
        UmlClass factura = createClass("com.universidad.modelo", "Factura");
        UmlClass bigDecimal = new UmlClass(
                "BigDecimal", new UmlNamespace(List.of("java", "math")), UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), Optional.of("external"), Optional.empty());
        UmlClass offsetDateTime = new UmlClass(
                "OffsetDateTime", new UmlNamespace(List.of("java", "time")), UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), Optional.of("external"), Optional.empty());
        UmlClass uuid = new UmlClass(
                "UUID", new UmlNamespace(List.of("java", "util")), UmlVisibility.PUBLIC,
                List.of(), Set.of(), List.of(), List.of(), Optional.of("external"), Optional.empty());

        Map<String, UmlClassifier> classifiers = new LinkedHashMap<>();
        classifiers.put(factura.qualifiedName(), factura);

        Set<UmlRelationship> relationships = new LinkedHashSet<>();
        relationships.add(new UmlAssociation(factura, "1", bigDecimal, "1", AggregationKind.NONE, Optional.empty(), Optional.empty()));
        relationships.add(new UmlAssociation(factura, "1", offsetDateTime, "1", AggregationKind.NONE, Optional.empty(), Optional.empty()));
        relationships.add(new UmlAssociation(factura, "1", uuid, "1", AggregationKind.NONE, Optional.empty(), Optional.empty()));

        UmlModel model = new UmlModel(classifiers, relationships);

        AnalysisConfiguration config = AnalysisConfiguration.builder(JavaLanguageVersion.JAVA_21)
                .includeJavaPackage("java.math.BigDecimal")
                .includeJavaPackage("java.time.OffsetDateTime")
                .includeJavaPackage("java.util.UUID")
                .build();

        Path tempFile = Files.createTempFile("puml_test_whitelist", ".puml");
        UmlModelPlantUmlExporter exporter = new UmlModelPlantUmlExporter();
        exporter.export(model, config, tempFile);

        String content = Files.readString(tempFile);
        Files.deleteIfExists(tempFile);

        if (!content.contains("package Java {")) {
            throw new AssertionError("Fallo: Se esperaba 'package Java {' en el puml generado:\n" + content);
        }
        if (!content.contains("package math {")) {
            throw new AssertionError("Fallo: Se esperaba subpaquete 'package math {' en el puml generado:\n" + content);
        }
        if (!content.contains("class BigDecimal <<external>> {")) {
            throw new AssertionError("Fallo: Se esperaba 'class BigDecimal <<external>> {' en el puml generado:\n" + content);
        }
        if (!content.contains("package time {")) {
            throw new AssertionError("Fallo: Se esperaba subpaquete 'package time {' en el puml generado:\n" + content);
        }
        if (!content.contains("class OffsetDateTime <<external>> {")) {
            throw new AssertionError("Fallo: Se esperaba 'class OffsetDateTime <<external>> {' en el puml generado:\n" + content);
        }
        if (!content.contains("package util {")) {
            throw new AssertionError("Fallo: Se esperaba subpaquete 'package util {' en el puml generado:\n" + content);
        }
        if (!content.contains("class UUID <<external>> {")) {
            throw new AssertionError("Fallo: Se esperaba 'class UUID <<external>> {' en el puml generado:\n" + content);
        }
        // Las relaciones hacia clases externas deben ser flechas punteadas con <<use>> y sin multiplicidades
        if (!content.contains("com.universidad.modelo.Factura ..> Java.math.BigDecimal : <<use>>")) {
            throw new AssertionError("Fallo: Relación punteada con BigDecimal no encontrada:\n" + content);
        }
        if (!content.contains("com.universidad.modelo.Factura ..> Java.time.OffsetDateTime : <<use>>")) {
            throw new AssertionError("Fallo: Relación punteada con OffsetDateTime no encontrada:\n" + content);
        }
        if (!content.contains("com.universidad.modelo.Factura ..> Java.util.UUID : <<use>>")) {
            throw new AssertionError("Fallo: Relación punteada con UUID no encontrada:\n" + content);
        }
        // Verificar que NO aparecen multiplicidades "1" en relaciones hacia clases externas
        if (content.contains("\"1\" --> \"1\" Java.")) {
            throw new AssertionError("Fallo: Se encontraron multiplicidades 1->1 hacia clases externas cuando no deben existir:\n" + content);
        }
    }
}
