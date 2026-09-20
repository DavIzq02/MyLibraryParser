package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.type.UmlType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class JavaTypeResolver {
            private final Map<String, UmlClassifier> classifiersByQualifiedName;
    private final Map<String, List<UmlClassifier>> classifiersBySimpleName;
    private final Set<String> javaPackageWhitelist;

    public JavaTypeResolver( Map<String, UmlClassifier> classifiers, List<String> javaPackageWhitelist) {
            this.classifiersByQualifiedName = Map.copyOf(classifiers);

        this.classifiersBySimpleName = classifiers.values()
                        .stream()
                        .collect(Collectors.groupingBy(UmlClassifier::name))
                        .entrySet()
                        .stream()
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue())));

        this.javaPackageWhitelist = javaPackageWhitelist == null
                        ? Set.of()
                        : Set.copyOf(javaPackageWhitelist);
    }

    public UmlType resolve( Type type, JavaTypeResolutionContext context) {
            if (type instanceof PrimitiveType primitiveType) {
            return new UmlType.Primitive(primitiveType.asString());
        }

        if (type instanceof VoidType) {
            return new UmlType.Primitive("void");
        }

        if (type instanceof ArrayType arrayType) {
            UmlType elementType = resolve(arrayType.getComponentType(), context);

            return new UmlType.Array(elementType, arrayType.getArrayLevel());
        }

        if (type instanceof ClassOrInterfaceType classType) {
            return resolveClassType(classType, context);
        }

        return new UmlType.Unknown(type.asString());
    }

    private UmlType resolveClassType( ClassOrInterfaceType classType, JavaTypeResolutionContext context) {
            String name = classType.getNameAsString();

        if (context.templateParameters().contains(name)) {
            return new UmlType.TemplateParameter(name);
        }

        UmlClassifier classifier = resolveClassifier(name, context);

        UmlType baseType;

        if (classifier != null) {
            baseType = new UmlType.Reference(classifier);

        } else {
            String externalQualifiedName = resolveKnownJavaType(name, context);

            if (externalQualifiedName != null) {
            baseType = new UmlType.ExternalReference(externalQualifiedName);

            } else {
            baseType = new UmlType.Unknown(name);
            }
        }

        if (classType.getTypeArguments().isEmpty()) {
            return baseType;
        }

        List<UmlType> typeArguments = new ArrayList<>();

        for (Type typeArgument : classType.getTypeArguments().get()) {
            typeArguments.add(resolve(typeArgument, context));
        }

        return new UmlType.Parameterized(baseType, typeArguments);
    }

    public JavaTypeResolutionContext createContext( CompilationUnit compilationUnit) {
            String packageName = compilationUnit.getPackageDeclaration()
                        .map(packageDeclaration ->
                                packageDeclaration.getNameAsString())
                        .orElse("");

        List<String> explicitImports = new ArrayList<>();
        List<String> wildcardImports = new ArrayList<>();

        compilationUnit.getImports().forEach(importDeclaration -> {
            String importName = importDeclaration.getNameAsString();

                    if (importDeclaration.isAsterisk()) {
                        wildcardImports.add(importName);
                    } else {
                        explicitImports.add(importName);
                    }
                });

        return new JavaTypeResolutionContext(packageName, explicitImports, wildcardImports, List.of());
    }

    private UmlClassifier resolveClassifier( String name, JavaTypeResolutionContext context) {
            UmlClassifier classifier = classifiersByQualifiedName.get(name);

        if (classifier != null) {
            return classifier;
        }

        classifier = resolveFromExplicitImports(name, context);

        if (classifier != null) {
            return classifier;
        }

        classifier = resolveFromCurrentPackage(name, context);

        if (classifier != null) {
            return classifier;
        }

        classifier = resolveFromWildcardImports(name, context);

        if (classifier != null) {
            return classifier;
        }

        List<UmlClassifier> candidates = classifiersBySimpleName.get(name);

        if (candidates == null
                || candidates.size() != 1) {
            return null;
        }

        return candidates.get(0);
    }

    private UmlClassifier resolveFromExplicitImports( String name, JavaTypeResolutionContext context) {
            for (String importedType : context.explicitImports()) {
            if (importedType.endsWith("." + name)) {
            UmlClassifier classifier = findByJavaQualifiedName(importedType);

                if (classifier != null) {
                    return classifier;
                }
            }
        }

        return null;
    }

    private UmlClassifier resolveFromCurrentPackage( String name, JavaTypeResolutionContext context) {
            if (context.packageName().isEmpty()) {
            return null;
        }

        return findByJavaQualifiedName(context.packageName() + "." + name);
    }

    private UmlClassifier resolveFromWildcardImports( String name, JavaTypeResolutionContext context) {
            for (String importedPackage : context.wildcardImports()) {
            UmlClassifier classifier = findByJavaQualifiedName(importedPackage + "." + name);

            if (classifier != null) {
                return classifier;
            }
        }

        return null;
    }

    private UmlClassifier findByJavaQualifiedName( String javaQualifiedName) {
            String umlQualifiedName = javaQualifiedName.replace(".", "::");

        return classifiersByQualifiedName.get(umlQualifiedName);
    }

    private String resolveKnownJavaType( String name, JavaTypeResolutionContext context) {
            String qualifiedName = resolveJavaQualifiedName(name, context);

        if (qualifiedName == null) {
            return null;
        }

        if (!javaPackageWhitelist.contains(qualifiedName)) {
            return null;
        }

        return qualifiedName;
    }

    private String resolveJavaQualifiedName( String name, JavaTypeResolutionContext context) {
            for (String importedType : context.explicitImports()) {
            if (importedType.endsWith("." + name)
                    && javaPackageWhitelist.contains(importedType)) {
            return importedType;
            }
        }

        for (String importedPackage : context.wildcardImports()) {
            String qualifiedName = importedPackage + "." + name;

            if (javaPackageWhitelist.contains(qualifiedName)) {
                return qualifiedName;
            }
        }

        String javaLangType = "java.lang." + name;

        if (javaPackageWhitelist.contains(javaLangType)) {
            return javaLangType;
        }

        return null;
    }
}