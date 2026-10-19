package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import generador.application.project.AnalysisConfiguration;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.model.UmlModel;
import generador.core.domain.relationship.UmlRelationship;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JavaProjectParser {

    private final JavaRelationshipParser relationshipParser;
    private final JavaNestingParser nestingParser;
    private final JavaSourceParser sourceParser;
    private final JavaClassifierParser classifierParser;
    private final AnalysisConfiguration configuration;

    public JavaProjectParser(AnalysisConfiguration configuration) {
        this.configuration = configuration;
        this.relationshipParser = new JavaRelationshipParser();
        this.nestingParser = new JavaNestingParser();
        this.sourceParser = new JavaSourceParser(configuration);
        this.classifierParser = new JavaClassifierParser(new JavaPropertyParser(), new JavaOperationParser(),
                configuration.excludedClasses());
    }

    public UmlModel parse(Path sourceDirectory) {
        if (!Files.exists(sourceDirectory))
            throw new IllegalArgumentException("El directorio fuente no existe: " + sourceDirectory);

        if (!Files.isDirectory(sourceDirectory))
            throw new IllegalArgumentException("La ruta indicada no es un directorio: " + sourceDirectory);

        List<CompilationUnit> compilationUnits = new ArrayList<>();

        try (Stream<Path> files = Files.walk(sourceDirectory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(javaFile -> {
                        CompilationUnit compilationUnit = sourceParser.parse(javaFile);
                        if (!isBlacklisted(compilationUnit)) compilationUnits.add(compilationUnit);
                    });
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "No fue posible recorrer el directorio fuente: " + sourceDirectory, exception);
        }

        List<UmlClassifier> classifiers = new ArrayList<>();
        for (CompilationUnit compilationUnit : compilationUnits)
            classifiers.addAll(classifierParser.parseStructure(compilationUnit));

        classifiers.stream().collect(Collectors.groupingBy(UmlClassifier::qualifiedName))
                .forEach((qualifiedName, duplicatedClassifiers) -> { });

        Map<String, UmlClassifier> classifierMap = classifiers.stream()
                .collect(Collectors.toMap(UmlClassifier::qualifiedName, Function.identity()));

        JavaTypeResolver typeResolver = new JavaTypeResolver(classifierMap, configuration.javaPackageWhitelist(),
                configuration.whiteList(), configuration.blackList());

        List<UmlClassifier> completeClassifiers = new ArrayList<>();
        for (CompilationUnit compilationUnit : compilationUnits)
            completeClassifiers.addAll(classifierParser.parse(compilationUnit, typeResolver));

        Map<String, UmlClassifier> completeClassifierMap = completeClassifiers.stream()
                .collect(Collectors.toMap(UmlClassifier::qualifiedName, Function.identity()));

        List<UmlRelationship> relationships = new ArrayList<>();
        for (CompilationUnit compilationUnit : compilationUnits) {
            relationships.addAll(relationshipParser.parse(compilationUnit, completeClassifierMap, typeResolver));
            relationships.addAll(nestingParser.parse(compilationUnit, completeClassifierMap));
        }

        return new UmlModel(Map.copyOf(completeClassifierMap), Set.copyOf(relationships));
    }

    private boolean isBlacklisted(CompilationUnit compilationUnit) {
        String packageName = compilationUnit.getPackageDeclaration()
                .map(packageDeclaration -> packageDeclaration.getNameAsString()).orElse("");

        for (String blacklistedPackage : configuration.projectPackageBlacklist()) {
            if (packageName.equals(blacklistedPackage)
                    || packageName.startsWith(blacklistedPackage + ".")) return true;
        }

        return false;
    }
}