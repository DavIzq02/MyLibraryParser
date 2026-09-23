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

    public JavaClassifierParser(JavaPropertyParser propertyParser, JavaOperationParser operationParser, Set<String> excludedClasses) {
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

        String className = declaration.getNameAsString();
        if (excludedClasses.contains(className) || excludedClasses.contains(declaration.getFullyQualifiedName().orElse(""))) return;

        /*
         * Este es el punto importante del cambio.
         *
         * Antes el contexto solamente conocía:
         * - package
         * - imports
         * - template parameters
         *
         * Ahora también conoce el tipo que estamos
         * analizando actualmente.
         */
        String currentTypeQualifiedName = namespace.qualifiedNameOf(className);
        JavaTypeResolutionContext classifierContext = context.withCurrentTypeQualifiedName(currentTypeQualifiedName);
        UmlClassifier classifier = null;

        if (declaration instanceof ClassOrInterfaceDeclaration classOrInterface) {
            classifier = classOrInterface.isInterface()
                    ? parseInterface(classOrInterface, namespace, typeResolver, classifierContext)
                    : parseClass(classOrInterface, namespace, typeResolver, classifierContext);
        } else if (declaration instanceof EnumDeclaration enumDeclaration) {
            classifier = parseEnumeration(enumDeclaration, namespace);
        } else if (declaration instanceof RecordDeclaration recordDeclaration) {
            classifier = parseRecord(recordDeclaration, namespace, typeResolver, classifierContext);
        } else if (declaration instanceof AnnotationDeclaration annotationDeclaration) {
            classifier = parseAnnotation(annotationDeclaration, namespace, typeResolver, classifierContext);
        }

        if (classifier == null) return;
        classifiers.add(classifier);
        UmlNamespace childNamespace = namespace.child(classifier.name());

        /*
         * Los tipos anidados reciben el contexto original.
         * Al entrar a cada tipo, parseType() vuelve a establecer
         * el currentTypeQualifiedName correspondiente.
         */
        for (BodyDeclarationType nestedType : nestedTypes(declaration))
            parseType(nestedType.declaration(), childNamespace, typeResolver, context, classifiers);
    }

    private void parseTypeStructure(TypeDeclaration<?> declaration, UmlNamespace namespace, List<UmlClassifier> classifiers) {
        String className = declaration.getNameAsString();
        if (excludedClasses.contains(className) || excludedClasses.contains(declaration.getFullyQualifiedName().orElse(""))) return;

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

        if (classifier == null) return;
        classifiers.add(classifier);
        UmlNamespace childNamespace = namespace.child(classifier.name());

        for (BodyDeclarationType nestedType : nestedTypes(declaration))
            parseTypeStructure(nestedType.declaration(), childNamespace, classifiers);
    }

    private List<BodyDeclarationType> nestedTypes(TypeDeclaration<?> declaration) {
        List<BodyDeclarationType> nestedTypes = new ArrayList<>();
        for (BodyDeclaration<?> member : declaration.getMembers())
            if (member instanceof TypeDeclaration<?> nestedType) nestedTypes.add(new BodyDeclarationType(nestedType));
        return nestedTypes;
    }

    private record BodyDeclarationType(TypeDeclaration<?> declaration) {}

    private UmlClass parseClass(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace, JavaTypeResolver typeResolver,
            JavaTypeResolutionContext context) {

        List<String> templateParameters = resolveTemplateParameters(declaration);
        JavaTypeResolutionContext classifierContext = context.withTemplateParameters(templateParameters);
        List<UmlProperty> properties = new ArrayList<>();
        Set<String> constructorParameters = resolveConstructorParameters(declaration);

        for (var field : declaration.getFields()) {
            List<UmlProperty> parsedProperties = propertyParser.parse(field, typeResolver, classifierContext, constructorParameters);
            properties.addAll(parsedProperties);
        }

        List<UmlOperation> operations = new ArrayList<>();
        for (var constructor : declaration.getConstructors())
            operations.add(operationParser.parseConstructor(constructor, typeResolver, classifierContext));

        for (var method : declaration.getMethods())
            operations.addAll(operationParser.parse(method, typeResolver, classifierContext));

        return new UmlClass(declaration.getNameAsString(), namespace, resolveVisibility(declaration), templateParameters,
                resolveModifiers(declaration), properties, operations, Optional.empty(), Optional.empty());
    }

    private Set<String> resolveConstructorParameters(ClassOrInterfaceDeclaration declaration) {
        Set<String> parameters = new java.util.HashSet<>();
        for (var constructor : declaration.getConstructors())
            for (Parameter parameter : constructor.getParameters()) parameters.add(parameter.getNameAsString());
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

        for (var method : declaration.getMethods())
            operations.addAll(operationParser.parse(method, typeResolver, classifierContext));

        return new UmlInterface(declaration.getNameAsString(), namespace, resolveVisibility(declaration), templateParameters,
                resolveModifiers(declaration), List.of(), operations, Optional.empty(), Optional.empty());
    }

    private UmlInterface parseInterfaceStructure(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace) {
        return new UmlInterface(declaration.getNameAsString(), namespace, resolveVisibility(declaration),
                resolveTemplateParameters(declaration), resolveModifiers(declaration), List.of(), List.of(),
                Optional.empty(), Optional.empty());
    }

    private UmlEnumeration parseEnumeration(EnumDeclaration declaration, UmlNamespace namespace) {
        List<UmlEnumerationLiteral> literals = new ArrayList<>();
        for (var entry : declaration.getEntries())
            literals.add(new UmlEnumerationLiteral(entry.getNameAsString()));

        return new UmlEnumeration(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(),
                resolveModifiers(declaration), List.of(), List.of(), literals, Optional.empty(), Optional.empty());
    }

    private UmlEnumeration parseEnumerationStructure(EnumDeclaration declaration, UmlNamespace namespace) {
        return new UmlEnumeration(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(),
                resolveModifiers(declaration), List.of(), List.of(), List.of(), Optional.empty(), Optional.empty());
    }

    private UmlRecord parseRecord(RecordDeclaration declaration, UmlNamespace namespace, JavaTypeResolver typeResolver,
            JavaTypeResolutionContext context) {

        JavaTypeResolutionContext classifierContext = context.withTemplateParameters(List.of());
        List<UmlProperty> properties = new ArrayList<>();

        /*
         * Los componentes del record son propiedades.
         */
        for (Parameter component : declaration.getParameters()) {
            properties.add(new UmlProperty(component.getNameAsString(), UmlVisibility.PRIVATE,
                    typeResolver.resolve(component.getType(), classifierContext), Set.of(UmlModifier.FINAL),
                    Optional.empty(), Optional.empty()));
        }

        List<UmlOperation> operations = new ArrayList<>();

        /*
         * 1. Constructores explícitos.
         */
        for (var constructor : declaration.getConstructors())
            operations.add(operationParser.parseConstructor(constructor, typeResolver, classifierContext));

        /*
         * 2. Constructor canónico implícito.
         *
         * JavaParser no expone el constructor que Java
         * genera automáticamente para el record.
         */
        if (declaration.getConstructors().isEmpty()) {
            List<UmlParameter> parameters = new ArrayList<>();

            for (Parameter component : declaration.getParameters())
                parameters.add(new UmlParameter(component.getNameAsString(),
                        typeResolver.resolve(component.getType(), classifierContext)));

            operations.add(new UmlOperation(declaration.getNameAsString(), resolveVisibility(declaration),
                    new UmlType.Primitive("void"), parameters, Set.of(), Optional.empty(), true));
        }

        /*
         * 3. Métodos explícitos.
         */
        for (var method : declaration.getMethods())
            operations.addAll(operationParser.parse(method, typeResolver, classifierContext));

        /*
         * 4. Accessors implícitos del record.
         */
        for (Parameter component : declaration.getParameters()) {
            boolean accessorExists = operations.stream().anyMatch(operation ->
                    !operation.isConstructor() && operation.name().equals(component.getNameAsString()) &&
                    operation.parameters().isEmpty());

            if (!accessorExists)
                operations.add(new UmlOperation(component.getNameAsString(), UmlVisibility.PUBLIC,
                        typeResolver.resolve(component.getType(), classifierContext), List.of(), Set.of(),
                        Optional.empty(), false));
        }

        return new UmlRecord(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(),
                resolveModifiers(declaration), properties, operations, Optional.empty(), Optional.empty());
    }

    private UmlRecord parseRecordStructure(RecordDeclaration declaration, UmlNamespace namespace) {
        return new UmlRecord(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(),
                resolveModifiers(declaration), List.of(), List.of(), Optional.empty(), Optional.empty());
    }

    private UmlAnnotation parseAnnotation(AnnotationDeclaration declaration, UmlNamespace namespace,
            JavaTypeResolver typeResolver, JavaTypeResolutionContext context) {

        List<UmlOperation> operations = new ArrayList<>();

        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof AnnotationMemberDeclaration annotationMember) {
                UmlType returnType = typeResolver.resolve(annotationMember.getType(), context.withTemplateParameters(List.of()));
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

    private UmlNamespace resolveNamespace(CompilationUnit compilationUnit) {
        if (compilationUnit.getPackageDeclaration().isEmpty()) return UmlNamespace.root();

        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        return UmlNamespace.of(packageName.split("\\."));
    }

    private UmlVisibility resolveVisibility(ClassOrInterfaceDeclaration declaration) {
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)) return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED)) return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE)) return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }

    private UmlVisibility resolveVisibility(EnumDeclaration declaration) {
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)) return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED)) return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE)) return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }

    private UmlVisibility resolveVisibility(RecordDeclaration declaration) {
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)) return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED)) return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE)) return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }

    private UmlVisibility resolveVisibility(AnnotationDeclaration declaration) {
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)) return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED)) return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE)) return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }

    private Set<UmlModifier> resolveModifiers(ClassOrInterfaceDeclaration declaration) {
        Set<UmlModifier> modifiers = new java.util.HashSet<>();
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT)) modifiers.add(UmlModifier.ABSTRACT);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL)) modifiers.add(UmlModifier.FINAL);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.STATIC)) modifiers.add(UmlModifier.STATIC);
        return Set.copyOf(modifiers);
    }

    private Set<UmlModifier> resolveModifiers(EnumDeclaration declaration) {
        Set<UmlModifier> modifiers = new java.util.HashSet<>();
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT)) modifiers.add(UmlModifier.ABSTRACT);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL)) modifiers.add(UmlModifier.FINAL);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.STATIC)) modifiers.add(UmlModifier.STATIC);
        return Set.copyOf(modifiers);
    }

    private Set<UmlModifier> resolveModifiers(RecordDeclaration declaration) {
        Set<UmlModifier> modifiers = new java.util.HashSet<>();
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT)) modifiers.add(UmlModifier.ABSTRACT);
        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL)) modifiers.add(UmlModifier.FINAL);
        return Set.copyOf(modifiers);
    }
}