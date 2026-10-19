package generador.infrastructure.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import generador.application.project.AnalysisConfiguration;
import generador.application.project.JavaLanguageVersion;
import generador.core.domain.classifier.UmlClassifier;

import java.util.List;

public class ClassExclusionParserTest {

    public static void main(String[] args) {
        testAnalysisConfigurationParameters();
        testAnalysisConfigurationBuilder();
        testJavaClassifierParserExclusion();
        System.out.println("TODOS LOS TESTS DE EXCLUSION DE CLASES PASARON SATISFACTORIAMENTE.");
    }

    private static void testAnalysisConfigurationParameters() {
        AnalysisConfiguration config = new AnalysisConfiguration(
                JavaLanguageVersion.JAVA_21,
                List.of("java.util.List"),
                List.of("com.ejemplo.vista"),
                List.of("ServicioIgnorado", "com.ejemplo.modelo.EntidadIgnorada")
        );

        if (config.projectClassBlacklist().size() != 2) {
            throw new AssertionError("projectClassBlacklist debería tener 2 elementos");
        }
        if (!config.excludedClasses().contains("ServicioIgnorado")) {
            throw new AssertionError("excludedClasses debería contener 'ServicioIgnorado'");
        }
        if (!config.excludedClasses().contains("com.ejemplo.modelo.EntidadIgnorada")) {
            throw new AssertionError("excludedClasses debería contener 'com.ejemplo.modelo.EntidadIgnorada'");
        }
    }

    private static void testAnalysisConfigurationBuilder() {
        AnalysisConfiguration config = AnalysisConfiguration.builder(JavaLanguageVersion.JAVA_21)
                .excludeClass("ClaseA")
                .excludeClasses(List.of("ClaseB", "ClaseC"))
                .build();

        if (config.projectClassBlacklist().size() != 3) {
            throw new AssertionError("projectClassBlacklist debería tener 3 elementos");
        }
        if (!config.excludedClasses().contains("ClaseA") || !config.excludedClasses().contains("ClaseB")
                || !config.excludedClasses().contains("ClaseC")) {
            throw new AssertionError("excludedClasses debería contener ClaseA, ClaseB y ClaseC");
        }
    }

    private static void testJavaClassifierParserExclusion() {
        String code = """
                package com.ejemplo.demo;

                public class ClaseValida {
                    private int id;
                }

                class ClaseExcluidaSimple {
                    private String nombre;
                }

                class ClaseExcluidaFqn {
                    private double total;
                }
                """;

        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();

        AnalysisConfiguration config = new AnalysisConfiguration(
                JavaLanguageVersion.JAVA_21,
                List.of(),
                List.of(),
                List.of("ClaseExcluidaSimple", "com.ejemplo.demo.ClaseExcluidaFqn")
        );

        JavaClassifierParser classifierParser = new JavaClassifierParser(
                new JavaPropertyParser(),
                new JavaOperationParser(),
                config.excludedClasses()
        );

        List<UmlClassifier> classifiers = classifierParser.parseStructure(cu);

        if (classifiers.size() != 1) {
            throw new AssertionError("Se esperaba 1 clasificador pero se obtuvieron: " + classifiers.size());
        }

        if (!classifiers.get(0).name().equals("ClaseValida")) {
            throw new AssertionError("El clasificador restante debería ser 'ClaseValida', pero fue: " + classifiers.get(0).name());
        }
    }
}
