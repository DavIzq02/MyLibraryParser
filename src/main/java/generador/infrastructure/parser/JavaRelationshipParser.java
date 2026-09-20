package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.classifier.UmlEnumeration;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.relationship.UmlAssociation;
import generador.core.domain.relationship.UmlDependency;
import generador.core.domain.relationship.UmlGeneralization;
import generador.core.domain.relationship.UmlRealization;
import generador.core.domain.relationship.UmlRelationship;
import generador.core.domain.spec.AggregationKind;
import generador.core.domain.type.UmlType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JavaRelationshipParser {
    public List<UmlRelationship> parse(CompilationUnit compilationUnit, Map<String, UmlClassifier> classifiers,
            JavaTypeResolver typeResolver) {
        JavaTypeResolutionContext context = typeResolver.createContext(compilationUnit);

        List<UmlRelationship> relationships = new ArrayList<>();

        for (ClassOrInterfaceDeclaration declaration : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
            UmlClassifier source = classifiers.get(resolveQualifiedName(declaration, context));

            if (source == null) {
                continue;
            }

            parseExtendedTypes(declaration, source, typeResolver, context, relationships);

            parseImplementedTypes(declaration, source, typeResolver, context, relationships);

            parseProperties(source, relationships);

            parseOperations(source, relationships);
        }

        return List.copyOf(relationships);
    }

    private void parseExtendedTypes(ClassOrInterfaceDeclaration declaration, UmlClassifier source,
            JavaTypeResolver typeResolver, JavaTypeResolutionContext context, List<UmlRelationship> relationships) {
        declaration.getExtendedTypes()
                .forEach(type -> {
                    UmlType resolvedType = typeResolver.resolve(type, context);

                    UmlClassifier target = resolveRelationshipTarget(resolvedType);

                    if (target != null) {
                        addRelationshipIfAbsent(
                                new UmlGeneralization(source, target, Optional.empty(), Optional.empty()),
                                relationships);
                    }
                });
    }

    private void parseImplementedTypes(ClassOrInterfaceDeclaration declaration, UmlClassifier source,
            JavaTypeResolver typeResolver, JavaTypeResolutionContext context, List<UmlRelationship> relationships) {
        declaration.getImplementedTypes()
                .forEach(type -> {
                    UmlType resolvedType = typeResolver.resolve(type, context);

                    UmlClassifier target = resolveRelationshipTarget(resolvedType);

                    if (target != null) {
                        addRelationshipIfAbsent(new UmlRealization(source, target, Optional.empty(), Optional.empty()),
                                relationships);
                    }
                });
    }

    private void parseProperties(UmlClassifier source, List<UmlRelationship> relationships) {
        for (UmlProperty property : source.properties()) {
            List<UmlClassifier> targets = resolveAssociationTargets(property.type());

            for (UmlClassifier target : targets) {
                addRelationshipIfAbsent(new UmlAssociation(source, "", target, "", property.aggregationKind()
                        .orElse(AggregationKind.NONE), Optional.empty(), Optional.empty()), relationships);
            }
        }
    }

    private void parseOperations(UmlClassifier source, List<UmlRelationship> relationships) {
        for (UmlOperation operation : source.operations()) {
            addDependencies(source, operation.returnType(), relationships);

            operation.parameters()
                    .forEach(parameter -> addDependencies(source, parameter.type(), relationships));
        }
    }

    private void addDependencies(UmlClassifier source, UmlType type, List<UmlRelationship> relationships) {
        List<UmlClassifier> targets = resolveDependencyTargets(type);

        for (UmlClassifier target : targets) {
            if (hasAssociation(source, target, relationships)) {
                continue;
            }

            addRelationshipIfAbsent(new UmlDependency(source, target, Optional.empty(), Optional.empty()),
                    relationships);
        }
    }

    private void addRelationshipIfAbsent(UmlRelationship relationship, List<UmlRelationship> relationships) {
        if (containsRelationship(relationship, relationships))
            return;
        relationships.add(relationship);
    }

    private boolean containsRelationship(UmlRelationship relationship, List<UmlRelationship> relationships) {
        for (UmlRelationship existing : relationships) {
            if (!existing.getClass().equals(relationship.getClass())) {
                continue;
            }

            if (!existing.source().qualifiedName().equals(relationship.source().qualifiedName())) {
                continue;
            }

            if (!existing.target().qualifiedName().equals(relationship.target().qualifiedName())) {
                continue;
            }

            return true;
        }

        return false;
    }

    private boolean hasAssociation(UmlClassifier source, UmlClassifier target, List<UmlRelationship> relationships) {
        for (UmlRelationship relationship : relationships) {
            if (!(relationship instanceof UmlAssociation)) {
                continue;
            }

            if (!relationship.source()
                    .qualifiedName()
                    .equals(source.qualifiedName())) {
                continue;
            }

            if (!relationship.target()
                    .qualifiedName()
                    .equals(target.qualifiedName())) {
                continue;
            }

            return true;
        }

        return false;
    }

    private List<UmlClassifier> resolveAssociationTargets(UmlType type) {
        List<UmlClassifier> targets = new ArrayList<>();

        if (type instanceof UmlType.Reference reference) {
            UmlClassifier classifier = reference.classifier();

            if (!(classifier instanceof UmlEnumeration)) {
                targets.add(classifier);
            }

            return targets;
        }

        if (type instanceof UmlType.Parameterized parameterized) {
            for (UmlType typeArgument : parameterized.typeArguments()) {
                targets.addAll(resolveAssociationTargets(typeArgument));
            }
        }

        return targets;
    }

    private List<UmlClassifier> resolveDependencyTargets(UmlType type) {
        List<UmlClassifier> targets = new ArrayList<>();

        if (type instanceof UmlType.Reference reference) {
            targets.add(reference.classifier());

            return targets;
        }

        if (type instanceof UmlType.Parameterized parameterized) {
            for (UmlType typeArgument : parameterized.typeArguments()) {
                targets.addAll(resolveDependencyTargets(typeArgument));
            }
        }

        return targets;
    }

    private UmlClassifier resolveRelationshipTarget(UmlType type) {
        if (type instanceof UmlType.Reference reference) {
            return reference.classifier();
        }

        if (type instanceof UmlType.Parameterized parameterized
                && parameterized.base() instanceof UmlType.Reference reference) {
            return reference.classifier();
        }

        return null;
    }

    private String resolveQualifiedName(ClassOrInterfaceDeclaration declaration, JavaTypeResolutionContext context) {
        String name = declaration.getNameAsString();

        if (context.packageName().isEmpty()) {
            return name;
        }

        return context.packageName()
                .replace(".", "::")
                + "::"
                + name;
    }
}