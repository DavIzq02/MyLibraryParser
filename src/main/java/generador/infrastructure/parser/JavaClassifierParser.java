package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import generador.core.domain.classifier.UmlAnnotation;
import generador.core.domain.classifier.UmlClass;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.classifier.UmlEnumeration;
import generador.core.domain.classifier.UmlEnumerationLiteral;
import generador.core.domain.classifier.UmlInterface;
import generador.core.domain.classifier.UmlRecord;
import generador.core.domain.element.UmlNamespace;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlParameter;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;
import generador.core.domain.type.UmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class JavaClassifierParser {

    private final JavaPropertyParser propertyParser;
    private final JavaOperationParser operationParser;
    private final Set<String> excludedClasses;

    public JavaClassifierParser(JavaPropertyParser propertyParser, JavaOperationParser operationParser,
            Set<String> excludedClasses) {
        this.propertyParser = propertyParser;
        this.operationParser = operationParser;
        this.excludedClasses = excludedClasses == null ? Set.of() : Set.copyOf(excludedClasses);
    }

    public List<UmlClassifier> parse(CompilationUnit compilationUnit, JavaTypeResolver typeResolver) {
        UmlNamespace namespace = resolveNamespace(compilationUnit);
        JavaTypeResolutionContext context = typeResolver.createContext(compilationUnit);
        List<UmlClassifier> classifiers = new ArrayList<>();
        for (TypeDeclaration<?> declaration : compilationUnit.getTypes())
            parseType(declaration, namespace, typeResolver, context, classifiers);
        return List.copyOf(classifiers);
    }

    public List<UmlClassifier> parseStructure(CompilationUnit compilationUnit) {
        UmlNamespace namespace = resolveNamespace(compilationUnit);
        List<UmlClassifier> classifiers = new ArrayList<>();
        for (TypeDeclaration<?> declaration : compilationUnit.getTypes())
            parseTypeStructure(declaration, namespace, classifiers);
        return List.copyOf(classifiers);
    }

    private void parseType(TypeDeclaration<?> declaration, UmlNamespace namespace, JavaTypeResolver typeResolver,
            JavaTypeResolutionContext context, List<UmlClassifier> classifiers) {

        if (isExcluded(declaration))
            return;

        String className = declaration.getNameAsString();
        String currentTypeQualifiedName = namespace.qualifiedNameOf(className);
        JavaTypeResolutionContext classifierContext = context.withCurrentTypeQualifiedName(currentTypeQualifiedName);
        UmlClassifier classifier = null;

        if (declaration instanceof ClassOrInterfaceDeclaration classOrInterface) {
            classifier = classOrInterface.isInterface()
                    ? parseInterface(classOrInterface, namespace, typeResolver, classifierContext)
                    : parseClass(classOrInterface, namespace, typeResolver, classifierContext);
        } else if (declaration instanceof EnumDeclaration enumDeclaration) {
            classifier = parseEnumeration(enumDeclaration, namespace, typeResolver, classifierContext);
        } else if (declaration instanceof RecordDeclaration recordDeclaration) {
            classifier = parseRecord(recordDeclaration, namespace, typeResolver, classifierContext);
        } else if (declaration instanceof AnnotationDeclaration annotationDeclaration) {
            classifier = parseAnnotation(annotationDeclaration, namespace, typeResolver, classifierContext);
        }

        if (classifier == null)
            return;
        classifiers.add(classifier);
        UmlNamespace childNamespace = namespace.child(classifier.name());

        for (BodyDeclarationType nestedType : nestedTypes(declaration))
            parseType(nestedType.declaration(), childNamespace, typeResolver, context, classifiers);
    }

    private void parseTypeStructure(TypeDeclaration<?> declaration, UmlNamespace namespace,
            List<UmlClassifier> classifiers) {
        if (isExcluded(declaration))
            return;

        UmlClassifier classifier = null;

        if (declaration instanceof ClassOrInterfaceDeclaration classOrInterface) {
            classifier = classOrInterface.isInterface()
                    ? parseInterfaceStructure(classOrInterface, namespace)
                    : parseClassStructure(classOrInterface, namespace);
        } else if (declaration instanceof EnumDeclaration enumDeclaration) {
            classifier = parseEnumerationStructure(enumDeclaration, namespace);
        } else if (declaration instanceof RecordDeclaration recordDeclaration) {
            classifier = parseRecordStructure(recordDeclaration, namespace);
        } else if (declaration instanceof AnnotationDeclaration annotationDeclaration) {
            classifier = parseAnnotationStructure(annotationDeclaration, namespace);
        }

        if (classifier == null)
            return;
        classifiers.add(classifier);
        UmlNamespace childNamespace = namespace.child(classifier.name());

        for (BodyDeclarationType nestedType : nestedTypes(declaration))
            parseTypeStructure(nestedType.declaration(), childNamespace, classifiers);
    }

    private boolean isExcluded(TypeDeclaration<?> declaration) {
        if (excludedClasses.isEmpty())
            return false;
        String className = declaration.getNameAsString();
        if (excludedClasses.contains(className))
            return true;

        String fullyQualified = declaration.getFullyQualifiedName().orElse("");
        if (!fullyQualified.isEmpty()) {
            if (excludedClasses.contains(fullyQualified))
                return true;
            if (excludedClasses.contains(fullyQualified.replace(".", "::")))
                return true;
        }
        return false;
    }

    private List<BodyDeclarationType> nestedTypes(TypeDeclaration<?> declaration) {
        List<BodyDeclarationType> nestedTypes = new ArrayList<>();
        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof TypeDeclaration<?> nestedType) {
                nestedTypes.add(new BodyDeclarationType(nestedType));
            } else {
                for (TypeDeclaration<?> localType : member.findAll(TypeDeclaration.class)) {
                    if (!localType.equals(declaration)) {
                        Optional<TypeDeclaration> parentType = localType.findAncestor(TypeDeclaration.class);
                        if (parentType.isPresent() && parentType.get().equals(declaration)) {
                            nestedTypes.add(new BodyDeclarationType(localType));
                        }
                    }
                }
            }
        }
        return nestedTypes;
    }

    private record BodyDeclarationType(TypeDeclaration<?> declaration) {
    }

    private UmlClass parseClass(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace,
            JavaTypeResolver typeResolver,
            JavaTypeResolutionContext context) {

        List<String> templateParameters = resolveTemplateParameters(declaration);
        JavaTypeResolutionContext classifierContext = context.withTemplateParameters(templateParameters);
        List<UmlProperty> properties = new ArrayList<>();
        Set<String> constructorParameters = resolveConstructorParameters(declaration);

        for (var field : declaration.getFields()) {
            List<UmlProperty> parsedProperties = propertyParser.parse(field, typeResolver, classifierContext,
                    constructorParameters);
            properties.addAll(parsedProperties);
        }

        List<UmlOperation> operations = new ArrayList<>();
        for (var constructor : declaration.getConstructors())
            operations.add(operationParser.parseConstructor(constructor, typeResolver, classifierContext));

        for (var method : declaration.getMethods())
            operations.addAll(operationParser.parse(method, typeResolver, classifierContext));

        return new UmlClass(declaration.getNameAsString(), namespace, resolveVisibility(declaration),
                templateParameters,
                resolveModifiers(declaration), properties, operations, Optional.empty(), Optional.empty());
    }

    private Set<String> resolveConstructorParameters(ClassOrInterfaceDeclaration declaration) {
        Set<String> parameters = new java.util.HashSet<>();
        for (var constructor : declaration.getConstructors())
            for (Parameter parameter : constructor.getParameters())
                parameters.add(parameter.getNameAsString());
        return Set.copyOf(parameters);
    }

    private UmlClass parseClassStructure(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace) {
        return new UmlClass(declaration.getNameAsString(), namespace, resolveVisibility(declaration),
                resolveTemplateParameters(declaration), resolveModifiers(declaration), List.of(), List.of(),
                Optional.empty(), Optional.empty());
    }

    private UmlInterface parseInterface(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace,
            JavaTypeResolver typeResolver, JavaTypeResolutionContext context) {

        List<String> templateParameters = resolveTemplateParameters(declaration);
        JavaTypeResolutionContext classifierContext = context.withTemplateParameters(templateParameters);
        List<UmlOperation> operations = new ArrayList<>();
        List<UmlProperty> properties = new ArrayList<>();

        for (var field : declaration.getFields()) {
            properties.addAll(propertyParser.parse(field, typeResolver, classifierContext, Set.of()));
        }

        for (var method : declaration.getMethods())
            operations.addAll(operationParser.parse(method, typeResolver, classifierContext));

        return new UmlInterface(declaration.getNameAsString(), namespace, resolveVisibility(declaration),
                templateParameters,
                resolveModifiers(declaration), properties, operations, Optional.empty(), Optional.empty());
    }

    private UmlInterface parseInterfaceStructure(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace) {
        return new UmlInterface(declaration.getNameAsString(), namespace, resolveVisibility(declaration),
                resolveTemplateParameters(declaration), resolveModifiers(declaration), List.of(), List.of(),
                Optional.empty(), Optional.empty());
    }

    private UmlEnumeration parseEnumeration(EnumDeclaration declaration, UmlNamespace namespace,
            JavaTypeResolver typeResolver, JavaTypeResolutionContext context) {

        List<UmlEnumerationLiteral> literals = new ArrayList<>();
        for (var entry : declaration.getEntries())
            literals.add(new UmlEnumerationLiteral(entry.getNameAsString()));

        List<UmlProperty> properties = new ArrayList<>();
        Set<String> constructorParameters = resolveConstructorParameters(declaration);

        for (var field : declaration.getFields()) {
            List<UmlProperty> parsedProperties = propertyParser.parse(field, typeResolver, context,
                    constructorParameters);
            properties.addAll(parsedProperties);
        }

        List<UmlOperation> operations = new ArrayList<>();
        for (var constructor : declaration.getConstructors())
            operations.add(operationParser.parseConstructor(constructor, typeResolver, context));

        for (var method : declaration.getMethods())
            operations.addAll(operationParser.parse(method, typeResolver, context));

        return new UmlEnumeration(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(),
                resolveModifiers(declaration), properties, operations, literals, Optional.empty(), Optional.empty());
    }

    private Set<String> resolveConstructorParameters(EnumDeclaration declaration) {
        Set<String> parameters = new java.util.HashSet<>();
        for (var constructor : declaration.getConstructors())
            for (Parameter parameter : constructor.getParameters())
                parameters.add(parameter.getNameAsString());
        return Set.copyOf(parameters);
    }

    private UmlEnumeration parseEnumerationStructure(EnumDeclaration declaration, UmlNamespace namespace) {
        List<UmlEnumerationLiteral> literals = new ArrayList<>();
        for (var entry : declaration.getEntries())
            literals.add(new UmlEnumerationLiteral(entry.getNameAsString()));

        return new UmlEnumeration(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(),
                resolveModifiers(declaration), List.of(), List.of(), literals, Optional.empty(), Optional.empty());
    }

    private UmlRecord parseRecord(RecordDeclaration declaration, UmlNamespace namespace, JavaTypeResolver typeResolver,
            JavaTypeResolutionContext context) {

        List<String> templateParameters = resolveTemplateParameters(declaration);
        JavaTypeResolutionContext classifierContext = context.withTemplateParameters(templateParameters);
        List<UmlProperty> properties = new ArrayList<>();

        // 1. Extraer componentes del record (parámetros del encabezado)
        for (Parameter component : declaration.getParameters()) {
            properties.add(new UmlProperty(component.getNameAsString(), UmlVisibility.PRIVATE,
                    typeResolver.resolve(component.getType(), classifierContext), Set.of(UmlModifier.FINAL),
                    Optional.empty(), Optional.empty()));
        }

        // 2. Extraer campos explícitos (constantes static final dentro del record)
        for (var field : declaration.getFields()) {
            List<UmlProperty> parsedProperties = propertyParser.parse(field, typeResolver, classifierContext, Set.of());
            properties.addAll(parsedProperties);
        }

        List<UmlOperation> operations = new ArrayList<>();

        // 3. Constructores explícitos
        for (var constructor : declaration.getConstructors())
            operations.add(operationParser.parseConstructor(constructor, typeResolver, classifierContext));

        // 4. Métodos explícitos
        for (var method : declaration.getMethods())
            operations.addAll(operationParser.parse(method, typeResolver, classifierContext));

        return new UmlRecord(declaration.getNameAsString(), namespace, resolveVisibility(declaration), templateParameters,
                resolveModifiers(declaration), properties, operations, Optional.empty(), Optional.empty());
    }

    private UmlRecord parseRecordStructure(RecordDeclaration declaration, UmlNamespace namespace) {
        return new UmlRecord(declaration.getNameAsString(), namespace, resolveVisibility(declaration), resolveTemplateParameters(declaration),
                resolveModifiers(declaration), List.of(), List.of(), Optional.empty(), Optional.empty());
    }

    private UmlAnnotation parseAnnotation(AnnotationDeclaration declaration, UmlNamespace namespace,
            JavaTypeResolver typeResolver, JavaTypeResolutionContext context) {

        List<UmlOperation> operations = new ArrayList<>();

        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof AnnotationMemberDeclaration annotationMember) {
                UmlType returnType = typeResolver.resolve(annotationMember.getType(),
                        context.withTemplateParameters(List.of()));
                operations.add(new UmlOperation(annotationMember.getNameAsString(), UmlVisibility.PUBLIC, returnType,
                        List.of(), Set.of(), Optional.empty(), false));
            }
        }

        return new UmlAnnotation(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(),
                Set.of(), List.of(), operations, Optional.empty(), Optional.empty());
    }

    private UmlAnnotation parseAnnotationStructure(AnnotationDeclaration declaration, UmlNamespace namespace) {
        return new UmlAnnotation(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(),
                Set.of(), List.of(), List.of(), Optional.empty(), Optional.empty());
    }

    private List<String> resolveTemplateParameters(ClassOrInterfaceDeclaration declaration) {
        List<String> parameters = new ArrayList<>();
        declaration.getTypeParameters().forEach(parameter -> parameters.add(parameter.getNameAsString()));
        return List.copyOf(parameters);
    }

    // Sobrecarga para extraer los parámetros genéricos de un record
    private List<String> resolveTemplateParameters(RecordDeclaration declaration) {
        List<String> parameters = new ArrayList<>();
        declaration.getTypeParameters().forEach(parameter -> parameters.add(parameter.getNameAsString()));
        return List.copyOf(parameters);
    }

    private UmlNamespace resolveNamespace(CompilationUnit compilationUnit) {
        if (compilationUnit.getPackageDeclaration().isEmpty())
            return UmlNamespace.root();

        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        return UmlNamespace.of(packageName.split("\\."));
    }

    private UmlVisibility resolveVisibility(ClassOrInterfaceDeclaration declaration) {
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC))
            return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED))
            return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE))
            return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }

    private UmlVisibility resolveVisibility(EnumDeclaration declaration) {
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC))
            return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED))
            return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE))
            return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }

    private UmlVisibility resolveVisibility(RecordDeclaration declaration) {
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC))
            return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED))
            return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE))
            return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }

    private UmlVisibility resolveVisibility(AnnotationDeclaration declaration) {
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC))
            return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED))
            return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE))
            return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }
    
    private Set<UmlModifier> resolveModifiers(ClassOrInterfaceDeclaration declaration) {
        Set<UmlModifier> modifiers = new java.util.HashSet<>();
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT))
            modifiers.add(UmlModifier.ABSTRACT);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL))
            modifiers.add(UmlModifier.FINAL);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.STATIC))
            modifiers.add(UmlModifier.STATIC);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.SEALED))
            modifiers.add(UmlModifier.SEALED);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.NON_SEALED))
            modifiers.add(UmlModifier.NON_SEALED);
        return Set.copyOf(modifiers);
    }

    private Set<UmlModifier> resolveModifiers(EnumDeclaration declaration) {
        Set<UmlModifier> modifiers = new java.util.HashSet<>();
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT))
            modifiers.add(UmlModifier.ABSTRACT);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL))
            modifiers.add(UmlModifier.FINAL);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.STATIC))
            modifiers.add(UmlModifier.STATIC);
        return Set.copyOf(modifiers);
    }

    private Set<UmlModifier> resolveModifiers(RecordDeclaration declaration) {
        Set<UmlModifier> modifiers = new java.util.HashSet<>();
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT))
            modifiers.add(UmlModifier.ABSTRACT);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL))
            modifiers.add(UmlModifier.FINAL);
        return Set.copyOf(modifiers);
    }
}