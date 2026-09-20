package generador.infrastructure.parser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import generador.application.project.AnalysisConfiguration;
import generador.application.project.JavaLanguageVersion;

import java.nio.file.Path;

public final class JavaSourceParser {
            private final AnalysisConfiguration configuration;

    public JavaSourceParser(AnalysisConfiguration configuration) {
        this.configuration = configuration;
    }

    public CompilationUnit parse(Path sourceFile) {
            try {
            ParserConfiguration parserConfiguration = new ParserConfiguration()
                            .setLanguageLevel(resolveLanguageLevel(configuration.javaLanguageVersion()));

            StaticJavaParser.setConfiguration(parserConfiguration);

            return StaticJavaParser.parse(sourceFile);

        } catch (Exception exception) {
            throw new IllegalArgumentException("No fue posible analizar el archivo Java: "
                            + sourceFile, exception);
        }
    }

    private ParserConfiguration.LanguageLevel resolveLanguageLevel(JavaLanguageVersion version) {
            return switch (version) {
            case JAVA_8 ->
                    ParserConfiguration.LanguageLevel.JAVA_8;

            case JAVA_11 ->
                    ParserConfiguration.LanguageLevel.JAVA_11;

            case JAVA_17 ->
                    ParserConfiguration.LanguageLevel.JAVA_17;

            case JAVA_21 ->
                    ParserConfiguration.LanguageLevel.JAVA_21;

            case JAVA_25 ->
                    ParserConfiguration.LanguageLevel.JAVA_25;
        };
    }
}