package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.relationship.UmlNesting;
import generador.core.domain.relationship.UmlRelationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JavaNestingParser {
            public List<UmlRelationship> parse( CompilationUnit compilationUnit, Map<String, UmlClassifier> classifiers) {
            List<UmlRelationship> relationships = new ArrayList<>();

        for (ClassOrInterfaceDeclaration outerDeclaration :
                compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
            UmlClassifier outerClassifier = classifiers.get(resolveQualifiedName(outerDeclaration, compilationUnit));

            if (outerClassifier == null) {
                continue;
            }

            for (BodyDeclaration<?> member :
                    outerDeclaration.getMembers()) {
            if (!(member instanceof ClassOrInterfaceDeclaration)) {
                    continue;
                }

                ClassOrInterfaceDeclaration innerDeclaration = (ClassOrInterfaceDeclaration) member;

                UmlClassifier innerClassifier = classifiers.get(resolveQualifiedName(innerDeclaration, compilationUnit));

                if (innerClassifier == null) {
                    continue;
                }

                relationships.add(new UmlNesting(outerClassifier, innerClassifier, Optional.empty(), Optional.empty()));
            }
        }

        return List.copyOf(relationships);
    }

    private String resolveQualifiedName( ClassOrInterfaceDeclaration declaration, CompilationUnit compilationUnit) {
            String packageName = compilationUnit.getPackageDeclaration()
                        .map(packageDeclaration ->
                                packageDeclaration
                                        .getNameAsString())
                        .orElse("");

        StringBuilder qualifiedName = new StringBuilder();

        if (!packageName.isEmpty()) {
            qualifiedName.append(packageName.replace(".", "::"));

            qualifiedName.append("::");
        }

        List<ClassOrInterfaceDeclaration> parents = declaration.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(parent ->
                                getParents(parent))
                        .orElse(List.of());

        for (ClassOrInterfaceDeclaration parent :
                parents) {
            qualifiedName.append(parent.getNameAsString());

            qualifiedName.append("::");
        }

        qualifiedName.append(declaration.getNameAsString());

        return qualifiedName.toString();
    }

    private List<ClassOrInterfaceDeclaration> getParents( ClassOrInterfaceDeclaration declaration) {
            List<ClassOrInterfaceDeclaration> parents = new ArrayList<>();

        ClassOrInterfaceDeclaration current = declaration;

        while (current != null) {
            parents.add(0, current);

            current = current.findAncestor(ClassOrInterfaceDeclaration.class)
                            .orElse(null);
        }

        return parents;
    }
}
