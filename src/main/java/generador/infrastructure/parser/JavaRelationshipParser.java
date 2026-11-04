package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

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
            parseProperties(source, relationships, typeResolver);
            parseOperations(source, relationships, typeResolver);
        }

        for (EnumDeclaration declaration : compilationUnit.findAll(EnumDeclaration.class)) {
            UmlClassifier source = classifiers.get(resolveQualifiedName(declaration, context));

            if (source == null) {
                continue;
            }

            declaration.getImplementedTypes().forEach(type -> {
                UmlType resolvedType = typeResolver.resolve(type, context);
                UmlClassifier target = resolveRelationshipTarget(resolvedType, typeResolver);
                if (target != null) {
                    addRelationshipIfAbsent(new UmlRealization(source, target, Optional.empty(), Optional.empty()),
                            relationships);
                }
            });

            parseProperties(source, relationships, typeResolver);
            parseOperations(source, relationships, typeResolver);
        }

        for (RecordDeclaration declaration : compilationUnit.findAll(RecordDeclaration.class)) {
            UmlClassifier source = classifiers.get(resolveQualifiedName(declaration, context));

            if (source == null) {
                continue;
            }

            declaration.getImplementedTypes().forEach(type -> {
                UmlType resolvedType = typeResolver.resolve(type, context);
                UmlClassifier target = resolveRelationshipTarget(resolvedType, typeResolver);
                if (target != null) {
                    addRelationshipIfAbsent(new UmlRealization(source, target, Optional.empty(), Optional.empty()),
                            relationships);
                }
            });

            parseProperties(source, relationships, typeResolver);
            parseOperations(source, relationships, typeResolver);
        }

        return List.copyOf(relationships);
    }

    private void parseExtendedTypes(ClassOrInterfaceDeclaration declaration, UmlClassifier source,
            JavaTypeResolver typeResolver, JavaTypeResolutionContext context, List<UmlRelationship> relationships) {
        declaration.getExtendedTypes()
                .forEach(type -> {
                    UmlType resolvedType = typeResolver.resolve(type, context);
                    UmlClassifier target = resolveRelationshipTarget(resolvedType, typeResolver);

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
                    UmlClassifier target = resolveRelationshipTarget(resolvedType, typeResolver);

                    if (target != null) {
                        addRelationshipIfAbsent(new UmlRealization(source, target, Optional.empty(), Optional.empty()),
                                relationships);
                    }
                });
    }

    private void parseProperties(UmlClassifier source, List<UmlRelationship> relationships, JavaTypeResolver typeResolver) {
        for (UmlProperty property : source.properties()) {
            List<UmlClassifier> targets = resolveAssociationTargets(property.type(), typeResolver);

            for (UmlClassifier target : targets) {
                // CASO 4 y Multiplicidad: Detecta arreglos/colecciones para inyectar "*"
                String sourceMult = "1";
                String targetMult = isMultiple(property.type()) ? "*" : "1";

                addRelationshipIfAbsent(new UmlAssociation(source, sourceMult, target, targetMult, property.aggregationKind()
                        .orElse(AggregationKind.NONE), Optional.empty(), Optional.empty()), relationships);
            }
        }
    }

    private void parseOperations(UmlClassifier source, List<UmlRelationship> relationships, JavaTypeResolver typeResolver) {
        for (UmlOperation operation : source.operations()) {
            // CASO 24: Extraer dependencias efímeras desde retornos y parámetros
            addDependencies(source, operation.returnType(), relationships, typeResolver);

            operation.parameters()
                    .forEach(parameter -> addDependencies(source, parameter.type(), relationships, typeResolver));
        }
    }

    private void addDependencies(UmlClassifier source, UmlType type, List<UmlRelationship> relationships, JavaTypeResolver typeResolver) {
        List<UmlClassifier> targets = resolveDependencyTargets(type, typeResolver);

        for (UmlClassifier target : targets) {
            // Evita auto-dependencias y PREVIENE que una dependencia sobreescriba la Herencia
            if (source.qualifiedName().equals(target.qualifiedName()) || hasStrongerRelationship(source, target, relationships)) {
                continue;
            }

            // Inyección de estereotipo <<use>> para dependencias efímeras.
            addRelationshipIfAbsent(new UmlDependency(source, target, Optional.of("<<use>>"), Optional.empty()),
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

    /**
     * Nuevo método que protege la Herencia y las Asociaciones fuertes.
     * Si ya existe un lazo estructural, evita registrar una dependencia redundante.
     */
    private boolean hasStrongerRelationship(UmlClassifier source, UmlClassifier target, List<UmlRelationship> relationships) {
        for (UmlRelationship relationship : relationships) {
            if (!relationship.source().qualifiedName().equals(source.qualifiedName())) {
                continue;
            }

            if (!relationship.target().qualifiedName().equals(target.qualifiedName())) {
                continue;
            }

            if (relationship instanceof UmlGeneralization || 
                relationship instanceof UmlRealization || 
                relationship instanceof UmlAssociation) {
                return true;
            }
        }

        return false;
    }

    private boolean isMultiple(UmlType type) {
        return (type instanceof UmlType.Array || type instanceof UmlType.Parameterized);
    }

    private List<UmlClassifier> resolveAssociationTargets(UmlType type, JavaTypeResolver typeResolver) {
        List<UmlClassifier> targets = new ArrayList<>();

        if (type instanceof UmlType.Reference reference) {
            UmlClassifier classifier = reference.classifier();
            if (!(classifier instanceof UmlEnumeration)) {
                targets.add(classifier);
            }
            return targets;
        }

        if (type instanceof UmlType.ExternalReference externalRef && typeResolver != null) {
            UmlClassifier classifier = typeResolver.resolveWhitelistedClassifier(externalRef.qualifiedName());
            if (classifier != null && !(classifier instanceof UmlEnumeration)) {
                targets.add(classifier);
            }
            return targets;
        }

        if (type instanceof UmlType.Parameterized parameterized) {
            if (parameterized.base() instanceof UmlType.ExternalReference externalBase && typeResolver != null) {
                UmlClassifier baseClassifier = typeResolver.resolveWhitelistedClassifier(externalBase.qualifiedName());
                if (baseClassifier != null && !(baseClassifier instanceof UmlEnumeration)) {
                    targets.add(baseClassifier);
                }
            }
            for (UmlType typeArgument : parameterized.typeArguments()) {
                targets.addAll(resolveAssociationTargets(typeArgument, typeResolver));
            }
        }

        // Desempaqueta también si es Array (varargs o [])
        if (type instanceof UmlType.Array arrayType) {
            targets.addAll(resolveAssociationTargets(arrayType.elementType(), typeResolver));
        }

        return targets;
    }

    private List<UmlClassifier> resolveDependencyTargets(UmlType type, JavaTypeResolver typeResolver) {
        List<UmlClassifier> targets = new ArrayList<>();

        if (type instanceof UmlType.Reference reference) {
            targets.add(reference.classifier());
            return targets;
        }

        if (type instanceof UmlType.ExternalReference externalRef && typeResolver != null) {
            UmlClassifier classifier = typeResolver.resolveWhitelistedClassifier(externalRef.qualifiedName());
            if (classifier != null) {
                targets.add(classifier);
            }
            return targets;
        }

        if (type instanceof UmlType.Parameterized parameterized) {
            if (parameterized.base() instanceof UmlType.ExternalReference externalBase && typeResolver != null) {
                UmlClassifier baseClassifier = typeResolver.resolveWhitelistedClassifier(externalBase.qualifiedName());
                if (baseClassifier != null) {
                    targets.add(baseClassifier);
                }
            }
            for (UmlType typeArgument : parameterized.typeArguments()) {
                targets.addAll(resolveDependencyTargets(typeArgument, typeResolver));
            }
        }

        if (type instanceof UmlType.Array arrayType) {
            targets.addAll(resolveDependencyTargets(arrayType.elementType(), typeResolver));
        }

        return targets;
    }

    private UmlClassifier resolveRelationshipTarget(UmlType type, JavaTypeResolver typeResolver) {
        if (type instanceof UmlType.Reference reference) {
            return reference.classifier();
        }

        if (type instanceof UmlType.ExternalReference externalRef && typeResolver != null) {
            return typeResolver.resolveWhitelistedClassifier(externalRef.qualifiedName());
        }

        if (type instanceof UmlType.Parameterized parameterized) {
            return resolveRelationshipTarget(parameterized.base(), typeResolver);
        }

        return null;
    }

    private String resolveQualifiedName(TypeDeclaration<?> declaration, JavaTypeResolutionContext context) {
        List<String> chain = new ArrayList<>();
        chain.add(declaration.getNameAsString());

        TypeDeclaration<?> ancestor = declaration.findAncestor(TypeDeclaration.class).orElse(null);
        while (ancestor != null) {
            chain.add(0, ancestor.getNameAsString());
            ancestor = ancestor.findAncestor(TypeDeclaration.class).orElse(null);
        }

        String simplePath = String.join("::", chain);

        if (context.packageName().isEmpty()) {
            return simplePath;
        }

        return context.packageName().replace(".", "::") + "::" + simplePath;
    }

    private String resolveQualifiedName(ClassOrInterfaceDeclaration declaration, JavaTypeResolutionContext context) {
        return resolveQualifiedName((TypeDeclaration<?>) declaration, context);
    }

    private String resolveQualifiedName(EnumDeclaration declaration, JavaTypeResolutionContext context) {
        return resolveQualifiedName((TypeDeclaration<?>) declaration, context);
    }

    private String resolveQualifiedName(RecordDeclaration declaration, JavaTypeResolutionContext context) {
        return resolveQualifiedName((TypeDeclaration<?>) declaration, context);
    }
}
