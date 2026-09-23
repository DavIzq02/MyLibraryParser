package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
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
        if (type instanceof PrimitiveType primitiveType) return new UmlType.Primitive(primitiveType.asString());
        if (type instanceof VoidType) return new UmlType.Primitive("void");

        if (type instanceof ArrayType arrayType) {
            UmlType elementType = resolve(arrayType.getComponentType(), context);
            return new UmlType.Array(elementType, arrayType.getArrayLevel());
        }

        if (type instanceof ClassOrInterfaceType classType) return resolveClassType(classType, context);
        return new UmlType.Unknown(type.asString());
    }

    private UmlType resolveClassType(ClassOrInterfaceType classType, JavaTypeResolutionContext context) {
        String name = classType.getNameAsString();
        if (context.templateParameters().contains(name)) return new UmlType.TemplateParameter(name);

        UmlClassifier classifier = resolveClassifier(name, context);
        UmlType baseType;

        if (classifier != null) {
            baseType = new UmlType.Reference(classifier);
        } else {
            String externalQualifiedName = resolveKnownJavaType(name, context);
            if (externalQualifiedName != null)
                baseType = new UmlType.ExternalReference(externalQualifiedName);
            else
                baseType = new UmlType.Unknown(name);
        }

        if (classType.getTypeArguments().isEmpty()) return baseType;

        List<UmlType> typeArguments = new ArrayList<>();
        for (Type typeArgument : classType.getTypeArguments().get())
            typeArguments.add(resolve(typeArgument, context));

        return new UmlType.Parameterized(baseType, typeArguments);
    }

    public JavaTypeResolutionContext createContext(CompilationUnit compilationUnit) {
        String packageName = compilationUnit.getPackageDeclaration()
                .map(packageDeclaration -> packageDeclaration.getNameAsString()).orElse("");

        List<String> explicitImports = new ArrayList<>();
        List<String> wildcardImports = new ArrayList<>();

        compilationUnit.getImports().forEach(importDeclaration -> {
            String importName = importDeclaration.getNameAsString();
            if (importDeclaration.isAsterisk()) wildcardImports.add(importName);
            else explicitImports.add(importName);
        });

        return new JavaTypeResolutionContext(packageName, explicitImports, wildcardImports, List.of(), "");
    }

    private UmlClassifier resolveClassifier(String name, JavaTypeResolutionContext context) {
        /*
         * 1. Nombre ya completamente calificado.
         */
        UmlClassifier classifier = classifiersByQualifiedName.get(name);
        if (classifier != null) return classifier;

        /*
         * 2. Tipos anidados/enclosing types.
         *
         * Ejemplo:
         *
         * current =
         * org::poo::prueba::RecordsTest::Persona
         *
         * name = Direccion
         *
         * Se prueban:
         *
         * org::poo::prueba::RecordsTest::Persona::Direccion
         *
         * org::poo::prueba::RecordsTest::Direccion
         *
         * org::poo::prueba::Direccion
         *
         * ...
         *
         * De esta manera se puede encontrar el sibling:
         *
         * RecordsTest::Direccion
         */
        classifier = resolveFromEnclosingTypes(name, context);
        if (classifier != null) return classifier;

        /*
         * 3. Import explícito.
         */
        classifier = resolveFromExplicitImports(name, context);
        if (classifier != null) return classifier;

        /*
         * 4. Tipo del package actual.
         */
        classifier = resolveFromCurrentPackage(name, context);
        if (classifier != null) return classifier;

        /*
         * 5. Import wildcard.
         */
        classifier = resolveFromWildcardImports(name, context);
        if (classifier != null) return classifier;

        /*
         * 6. Último recurso:
         * nombre simple sin ambigüedad.
         */
        List<UmlClassifier> candidates = classifiersBySimpleName.get(name);
        if (candidates == null || candidates.size() != 1) return null;
        return candidates.get(0);
    }

    private UmlClassifier resolveFromEnclosingTypes(String name, JavaTypeResolutionContext context) {
        String currentType = context.currentTypeQualifiedName();
        if (currentType == null || currentType.isBlank()) return null;

        while (currentType != null && !currentType.isBlank()) {
            String candidateQualifiedName = currentType + "::" + name;
            UmlClassifier classifier = classifiersByQualifiedName.get(candidateQualifiedName);
            if (classifier != null) return classifier;

            int separator = currentType.lastIndexOf("::");
            if (separator < 0) break;
            currentType = currentType.substring(0, separator);
        }

        return null;
    }

    private UmlClassifier resolveFromExplicitImports(String name, JavaTypeResolutionContext context) {
        for (String importedType : context.explicitImports()) {
            if (importedType.endsWith("." + name)) {
                UmlClassifier classifier = findByJavaQualifiedName(importedType);
                if (classifier != null) return classifier;
            }
        }

        return null;
    }

    private UmlClassifier resolveFromCurrentPackage(String name, JavaTypeResolutionContext context) {
        if (context.packageName().isEmpty()) return null;
        return findByJavaQualifiedName(context.packageName() + "." + name);
    }

    private UmlClassifier resolveFromWildcardImports(String name, JavaTypeResolutionContext context) {
        for (String importedPackage : context.wildcardImports()) {
            UmlClassifier classifier = findByJavaQualifiedName(importedPackage + "." + name);
            if (classifier != null) return classifier;
        }

        return null;
    }

    private UmlClassifier findByJavaQualifiedName(String javaQualifiedName) {
        String umlQualifiedName = javaQualifiedName.replace(".", "::");
        return classifiersByQualifiedName.get(umlQualifiedName);
    }

    private String resolveKnownJavaType(String name, JavaTypeResolutionContext context) {
        String qualifiedName = resolveJavaQualifiedName(name, context);
        if (qualifiedName == null) return null;

        /*
         * BlackList tiene prioridad.
         */
        if (blackList.contains(qualifiedName)) return null;

        /*
         * WhiteList permite explícitamente
         * el tipo.
         */
        if (whiteList.contains(qualifiedName)) return qualifiedName;

        /*
         * Tipos estándar de Java.
         */
        if (isStandardJavaType(qualifiedName)) return qualifiedName;

        /*
         * Compatibilidad con la configuración
         * anterior.
         */
        if (javaPackageWhitelist.contains(qualifiedName)) return qualifiedName;

        return null;
    }

    private String resolveJavaQualifiedName(String name, JavaTypeResolutionContext context) {
        /*
         * 1. Import explícito.
         */
        for (String importedType : context.explicitImports()) {
            if (importedType.endsWith("." + name)) return importedType;
        }

        /*
         * 2. Tipos estándar conocidos.
         */
        String[] standardPackages = {
            "java.lang.", "java.util.", "java.math.", "java.time."
        };

        for (String packagePrefix : standardPackages) {
            String qualifiedName = packagePrefix + name;
            if (isStandardJavaType(qualifiedName)) return qualifiedName;
        }

        /*
         * 3. Import wildcard.
         */
        for (String importedPackage : context.wildcardImports()) {
            String qualifiedName = importedPackage + "." + name;
            return qualifiedName;
        }

        /*
         * 4. Por defecto se intenta java.lang.
         */
        return "java.lang." + name;
    }

    private boolean isStandardJavaType(String qualifiedName) {
        return switch (qualifiedName) {
            // java.lang
            case "java.lang.String", "java.lang.Integer", "java.lang.Long", "java.lang.Short",
                 "java.lang.Byte", "java.lang.Boolean", "java.lang.Double", "java.lang.Float",
                 "java.lang.Character", "java.lang.Object", "java.lang.Number", "java.lang.Void",
                 "java.lang.Class", "java.lang.Enum", "java.lang.Throwable", "java.lang.Exception",
                 "java.lang.RuntimeException",
            // java.util
            "java.util.UUID", "java.util.List", "java.util.Set", "java.util.Map", "java.util.Collection",
            "java.util.ArrayList", "java.util.HashMap", "java.util.HashSet", "java.util.LinkedList",
            "java.util.LinkedHashSet", "java.util.Optional",
            // java.math
            "java.math.BigDecimal", "java.math.BigInteger",
            // java.time
            "java.time.LocalDate", "java.time.LocalDateTime", "java.time.LocalTime",
            "java.time.OffsetDateTime", "java.time.OffsetTime", "java.time.ZonedDateTime",
            "java.time.Instant", "java.time.Year", "java.time.Month",
            // java.util.Date
            "java.util.Date", "java.util.Calendar" -> true;

            default -> false;
        };
    }
}