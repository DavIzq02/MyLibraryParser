package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import generador.application.project.BlackList;
import generador.application.project.WhiteList;
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
    private final WhiteList whiteList;
    private final BlackList blackList;

    public JavaTypeResolver(Map<String, UmlClassifier> classifiers, List<String> javaPackageWhitelist,
            WhiteList whiteList, BlackList blackList) {
        this.classifiersByQualifiedName = Map.copyOf(classifiers);
        this.classifiersBySimpleName = classifiers.values().stream()
                .collect(Collectors.groupingBy(UmlClassifier::name)).entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue())));
        this.javaPackageWhitelist = javaPackageWhitelist == null ? Set.of() : Set.copyOf(javaPackageWhitelist);
        this.whiteList = whiteList == null ? new WhiteList() : whiteList;
        this.blackList = blackList == null ? new BlackList() : blackList;
    }

    public UmlType resolve(Type type, JavaTypeResolutionContext context) {
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

        // CASO 8: Wildcards (? extends / ? super). Extrae el tipo acotado en lugar de fallar.
        if (type instanceof WildcardType wildcardType) {
            if (wildcardType.getExtendedType().isPresent()) {
                return resolve(wildcardType.getExtendedType().get(), context);
            }
            if (wildcardType.getSuperType().isPresent()) {
                return resolve(wildcardType.getSuperType().get(), context);
            }
            return new UmlType.Unknown("?");
        }

        if (type instanceof ClassOrInterfaceType classType) {
            return resolveClassType(classType, context);
        }

        return new UmlType.Unknown(type.asString());
    }

    private UmlType resolveClassType(ClassOrInterfaceType classType, JavaTypeResolutionContext context) {
        String name = classType.getNameAsString();

        // CASO 9: Evitar confundir genéricos (ej. T) con clases del dominio.
        if (context.templateParameters().contains(name)) {
            return new UmlType.TemplateParameter(name);
        }

        // 1. Intentar resolver con exactitud basada en alcance léxico e imports reales.
        UmlClassifier classifier = resolveExactClassifier(name, context);
        if (classifier != null) {
            return buildParameterizedType(new UmlType.Reference(classifier), classType, context);
        }

        // 2. CASO 12 (Type Shadowing): Procesar tipos del JDK *antes* del fallback global.
        // Evita que un com.mi.String global sobreescriba un java.lang.String legítimo.
        String externalQualifiedName = resolveKnownJavaType(name, context);
        if (externalQualifiedName != null) {
            UmlType baseType = new UmlType.ExternalReference(externalQualifiedName);
            return buildParameterizedType(baseType, classType, context);
        }

        // 3. Fallback: Si no hay conflictos y falló todo lo anterior, buscar globalmente.
        List<UmlClassifier> candidates = classifiersBySimpleName.get(name);
        if (candidates != null && candidates.size() == 1) {
            return buildParameterizedType(new UmlType.Reference(candidates.get(0)), classType, context);
        }

        return buildParameterizedType(new UmlType.Unknown(name), classType, context);
    }

    // CASO 7: Desempaquetado recursivo de genéricos profundos en colecciones.
    private UmlType buildParameterizedType(UmlType baseType, ClassOrInterfaceType classType, JavaTypeResolutionContext context) {
        if (classType.getTypeArguments().isEmpty()) {
            return baseType;
        }

        List<UmlType> typeArguments = new ArrayList<>();
        for (Type typeArgument : classType.getTypeArguments().get()) {
            typeArguments.add(resolve(typeArgument, context));
        }

        return new UmlType.Parameterized(baseType, typeArguments);
    }

    public JavaTypeResolutionContext createContext(CompilationUnit compilationUnit) {
        String packageName = compilationUnit.getPackageDeclaration()
                .map(packageDeclaration -> packageDeclaration.getNameAsString()).orElse("");

        List<String> explicitImports = new ArrayList<>();
        List<String> wildcardImports = new ArrayList<>();

        // CASO 13: Incluir java.lang como import implícito estándar de Java.
        wildcardImports.add("java.lang");

        compilationUnit.getImports().forEach(importDeclaration -> {
            String importName = importDeclaration.getNameAsString();
            if (importDeclaration.isAsterisk()) {
                wildcardImports.add(importName);
            } else {
                explicitImports.add(importName);
            }
        });

        return new JavaTypeResolutionContext(packageName, explicitImports, wildcardImports, List.of(), "");
    }

    // Renombrado para diferenciarlo de la búsqueda ambigua. No incluye la búsqueda por simpleName.
    private UmlClassifier resolveExactClassifier(String name, JavaTypeResolutionContext context) {
        UmlClassifier classifier = classifiersByQualifiedName.get(name);
        if (classifier != null) {
            return classifier;
        }

        classifier = resolveFromEnclosingTypes(name, context);
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

        return resolveFromWildcardImports(name, context);
    }

    private UmlClassifier resolveFromEnclosingTypes(String name, JavaTypeResolutionContext context) {
        String currentType = context.currentTypeQualifiedName();
        if (currentType == null || currentType.isBlank()) {
            return null;
        }

        while (currentType != null && !currentType.isBlank()) {
            String candidateQualifiedName = currentType + "::" + name;
            UmlClassifier classifier = classifiersByQualifiedName.get(candidateQualifiedName);
            if (classifier != null) {
                return classifier;
            }

            int separator = currentType.lastIndexOf("::");
            if (separator < 0) {
                break;
            }
            currentType = currentType.substring(0, separator);
        }

        return null;
    }

    private UmlClassifier resolveFromExplicitImports(String name, JavaTypeResolutionContext context) {
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

    private UmlClassifier resolveFromCurrentPackage(String name, JavaTypeResolutionContext context) {
        if (context.packageName().isEmpty()) {
            return null;
        }
        return findByJavaQualifiedName(context.packageName() + "." + name);
    }

    private UmlClassifier resolveFromWildcardImports(String name, JavaTypeResolutionContext context) {
        for (String importedPackage : context.wildcardImports()) {
            UmlClassifier classifier = findByJavaQualifiedName(importedPackage + "." + name);
            if (classifier != null) {
                return classifier;
            }
        }
        return null;
    }

    private UmlClassifier findByJavaQualifiedName(String javaQualifiedName) {
        String umlQualifiedName = javaQualifiedName.replace(".", "::");
        return classifiersByQualifiedName.get(umlQualifiedName);
    }

    private String resolveKnownJavaType(String name, JavaTypeResolutionContext context) {
        String qualifiedName = resolveJavaQualifiedName(name, context);
        if (qualifiedName == null) {
            return null;
        }

        if (blackList.contains(qualifiedName)) {
            return null;
        }
        if (whiteList.contains(qualifiedName)) {
            return qualifiedName;
        }
        if (isStandardJavaType(qualifiedName)) {
            return qualifiedName;
        }
        if (javaPackageWhitelist.contains(qualifiedName)) {
            return qualifiedName;
        }

        return null;
    }

    private String resolveJavaQualifiedName(String name, JavaTypeResolutionContext context) {
        for (String importedType : context.explicitImports()) {
            if (importedType.endsWith("." + name)) {
                return importedType;
            }
        }

        String[] standardPackages = {"java.lang.", "java.util.", "java.math.", "java.time."};
        for (String packagePrefix : standardPackages) {
            String qualifiedName = packagePrefix + name;
            if (isStandardJavaType(qualifiedName)) {
                return qualifiedName;
            }
        }

        for (String importedPackage : context.wildcardImports()) {
            return importedPackage + "." + name;
        }

        return "java.lang." + name;
    }

    private boolean isStandardJavaType(String qualifiedName) {
        return switch (qualifiedName) {
            case "java.lang.String", "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.Byte", "java.lang.Boolean", "java.lang.Double", "java.lang.Float", "java.lang.Character", "java.lang.Object", "java.lang.Number", "java.lang.Void", "java.lang.Class", "java.lang.Enum", "java.lang.Throwable", "java.lang.Exception", "java.lang.RuntimeException", "java.util.UUID", "java.util.List", "java.util.Set", "java.util.Map", "java.util.Collection", "java.util.ArrayList", "java.util.HashMap", "java.util.HashSet", "java.util.LinkedList", "java.util.LinkedHashSet", "java.util.Optional", "java.math.BigDecimal", "java.math.BigInteger", "java.time.LocalDate", "java.time.LocalDateTime", "java.time.LocalTime", "java.time.OffsetDateTime", "java.time.OffsetTime", "java.time.ZonedDateTime", "java.time.Instant", "java.time.Year", "java.time.Month", "java.util.Date", "java.util.Calendar" ->
                true;
            default ->
                false;
        };
    }
}
