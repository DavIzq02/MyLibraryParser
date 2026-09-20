package generador.infrastructure.parser;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.Parameter;

import generador.core.domain.classifier.UmlEnumerationLiteral;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.classifier.UmlClass;
import generador.core.domain.classifier.UmlEnumeration;
import generador.core.domain.classifier.UmlInterface;
import generador.core.domain.classifier.UmlRecord;
import generador.core.domain.element.UmlNamespace;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class JavaClassifierParser {
            private final JavaPropertyParser propertyParser;
    private final JavaOperationParser operationParser;

    public JavaClassifierParser(JavaPropertyParser propertyParser, JavaOperationParser operationParser) {
        this.propertyParser = propertyParser;
        this.operationParser = operationParser;
    }

    public List<UmlClassifier> parse(CompilationUnit compilationUnit, JavaTypeResolver typeResolver) {
            UmlNamespace namespace = resolveNamespace(compilationUnit);
        JavaTypeResolutionContext context = typeResolver.createContext(compilationUnit);
        List<UmlClassifier> classifiers = new ArrayList<>();

        for (TypeDeclaration<?> declaration : compilationUnit.getTypes()) {
            parseType(declaration, namespace, typeResolver, context, classifiers);
        }
        return List.copyOf(classifiers);
    }

    public List<UmlClassifier> parseStructure(CompilationUnit compilationUnit) {
            UmlNamespace namespace = resolveNamespace(compilationUnit);
        List<UmlClassifier> classifiers = new ArrayList<>();

        for (TypeDeclaration<?> declaration : compilationUnit.getTypes()) {
            parseTypeStructure(declaration, namespace, classifiers);
        }

        return List.copyOf(classifiers);
    }

    private void parseType(TypeDeclaration<?> declaration, UmlNamespace namespace, JavaTypeResolver typeResolver, JavaTypeResolutionContext context, List<UmlClassifier> classifiers) {
            UmlClassifier classifier = null;

        if (declaration instanceof ClassOrInterfaceDeclaration classOrInterface) {
            if (classOrInterface.isInterface()) {
                classifier = parseInterface(classOrInterface, namespace, typeResolver, context);
            } else {
                classifier = parseClass(classOrInterface, namespace, typeResolver, context);
            }

        } else if (declaration instanceof EnumDeclaration enumDeclaration) {
            classifier = parseEnumeration(enumDeclaration, namespace);
        } else if (declaration instanceof RecordDeclaration recordDeclaration) {
            classifier = parseRecord(recordDeclaration, namespace, typeResolver, context);
        }

        if (classifier == null)
            return;

        classifiers.add(classifier);

        if (declaration instanceof ClassOrInterfaceDeclaration classOrInterface) {
            UmlNamespace childNamespace = namespace.child(classOrInterface.getNameAsString());
            for (BodyDeclarationType nestedType : nestedTypes(classOrInterface)) {
                parseType(nestedType.declaration(), childNamespace, typeResolver, context, classifiers);
            }

        } else if (declaration instanceof EnumDeclaration enumDeclaration) {
            UmlNamespace childNamespace = namespace.child(enumDeclaration.getNameAsString());

            for (BodyDeclarationType nestedType : nestedTypes(enumDeclaration)) {
                parseType(nestedType.declaration(), childNamespace, typeResolver, context, classifiers);
            }

        } else if (declaration instanceof RecordDeclaration recordDeclaration) {
            UmlNamespace childNamespace = namespace.child(recordDeclaration.getNameAsString());

            for (BodyDeclarationType nestedType : nestedTypes(recordDeclaration)) {
                parseType(nestedType.declaration(), childNamespace, typeResolver, context, classifiers);
            }
        }
    }

    private void parseTypeStructure(TypeDeclaration<?> declaration, UmlNamespace namespace, List<UmlClassifier> classifiers) {
            UmlClassifier classifier = null;

        if (declaration instanceof ClassOrInterfaceDeclaration classOrInterface) {
            if (classOrInterface.isInterface()) {
                classifier = parseInterfaceStructure(classOrInterface, namespace);
            } else {
                classifier = parseClassStructure(classOrInterface, namespace);
            }

        } else if (declaration instanceof EnumDeclaration enumDeclaration) {
            classifier = parseEnumerationStructure(enumDeclaration, namespace);
        } else if (declaration instanceof RecordDeclaration recordDeclaration) {
            classifier = parseRecordStructure(recordDeclaration, namespace);
        }

        if (classifier == null)
            return;

        classifiers.add(classifier);

        if (declaration instanceof ClassOrInterfaceDeclaration classOrInterface) {
            UmlNamespace childNamespace = namespace.child(classOrInterface.getNameAsString());

            for (BodyDeclarationType nestedType : nestedTypes(classOrInterface)) {
                parseTypeStructure(nestedType.declaration(), childNamespace, classifiers);
            }

        } else if (declaration instanceof EnumDeclaration enumDeclaration) {
            UmlNamespace childNamespace = namespace.child(enumDeclaration.getNameAsString());

            for (BodyDeclarationType nestedType : nestedTypes(enumDeclaration)) {
                parseTypeStructure(nestedType.declaration(), childNamespace, classifiers);
            }

        } else if (declaration instanceof RecordDeclaration recordDeclaration) {
            UmlNamespace childNamespace = namespace.child(recordDeclaration.getNameAsString());

            for (BodyDeclarationType nestedType : nestedTypes(recordDeclaration)) {
                parseTypeStructure(nestedType.declaration(), childNamespace, classifiers);
            }
        }
    }

    private List<BodyDeclarationType> nestedTypes(ClassOrInterfaceDeclaration declaration) {
            List<BodyDeclarationType> nestedTypes = new ArrayList<>();

        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof ClassOrInterfaceDeclaration nestedClass) {
                nestedTypes.add(new BodyDeclarationType(nestedClass));

            } else if (member instanceof EnumDeclaration nestedEnum) {
                nestedTypes.add(new BodyDeclarationType(nestedEnum));

            } else if (member instanceof RecordDeclaration nestedRecord) {
                nestedTypes.add(new BodyDeclarationType(nestedRecord));
            }
        }

        return nestedTypes;
    }

    private List<BodyDeclarationType> nestedTypes(EnumDeclaration declaration) {
            List<BodyDeclarationType> nestedTypes = new ArrayList<>();

        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof ClassOrInterfaceDeclaration nestedClass) {
                nestedTypes.add(new BodyDeclarationType(nestedClass));

            } else if (member instanceof EnumDeclaration nestedEnum) {
                nestedTypes.add(new BodyDeclarationType(nestedEnum));

            } else if (member instanceof RecordDeclaration nestedRecord) {
                nestedTypes.add(new BodyDeclarationType(nestedRecord));
            }
        }

        return nestedTypes;
    }

    private List<BodyDeclarationType> nestedTypes(RecordDeclaration declaration) {
            List<BodyDeclarationType> nestedTypes = new ArrayList<>();

        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof ClassOrInterfaceDeclaration nestedClass) {
                nestedTypes.add(new BodyDeclarationType(nestedClass));

            } else if (member instanceof EnumDeclaration nestedEnum) {
                nestedTypes.add(new BodyDeclarationType(nestedEnum));

            } else if (member instanceof RecordDeclaration nestedRecord) {
                nestedTypes.add(new BodyDeclarationType(nestedRecord));
            }
        }

        return nestedTypes;
    }

    private record BodyDeclarationType(TypeDeclaration<?> declaration) {
    }

    private UmlClass parseClass(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace, JavaTypeResolver typeResolver, JavaTypeResolutionContext context) {
            JavaTypeResolutionContext classifierContext = context.withTemplateParameters(resolveTemplateParameters(declaration));
        List<UmlProperty> properties = new ArrayList<>();

        for (var field : declaration.getFields()) {
            properties.addAll(propertyParser.parse(field, typeResolver, classifierContext));
        }

        List<UmlOperation> operations = new ArrayList<>();

        for (var method : declaration.getMethods()) {
            operations.addAll(operationParser.parse(method, typeResolver, classifierContext));
        }

        return new UmlClass(declaration.getNameAsString(), namespace, resolveVisibility(declaration), resolveTemplateParameters(declaration), resolveModifiers(declaration), properties, operations, Optional.empty(), Optional.empty());
    }

    private UmlClass parseClassStructure(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace) {
            return new UmlClass(declaration.getNameAsString(), namespace, resolveVisibility(declaration), resolveTemplateParameters(declaration), resolveModifiers(declaration), List.of(), List.of(), Optional.empty(), Optional.empty());
    }

    private UmlInterface parseInterface(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace, JavaTypeResolver typeResolver, JavaTypeResolutionContext context) {
            JavaTypeResolutionContext classifierContext = context.withTemplateParameters(resolveTemplateParameters(declaration));
        List<UmlOperation> operations = new ArrayList<>();

        for (var method : declaration.getMethods()) {
            operations.addAll(operationParser.parse(method, typeResolver, classifierContext));
        }

        return new UmlInterface(declaration.getNameAsString(), namespace, resolveVisibility(declaration), resolveTemplateParameters(declaration), resolveModifiers(declaration), List.of(), operations, Optional.empty(), Optional.empty());
    }

    private UmlInterface parseInterfaceStructure(ClassOrInterfaceDeclaration declaration, UmlNamespace namespace) {
            return new UmlInterface(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(), resolveModifiers(declaration), List.of(), List.of(), Optional.empty(), Optional.empty());
    }

    private UmlEnumeration parseEnumeration(EnumDeclaration declaration, UmlNamespace namespace) {
            List<UmlEnumerationLiteral> literals = new ArrayList<>();

        for (var entry : declaration.getEntries()) {
            literals.add(new UmlEnumerationLiteral(entry.getNameAsString()));
        }

        return new UmlEnumeration(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(), resolveModifiers(declaration), List.of(), List.of(), literals, Optional.empty(), Optional.empty());
    }

    private UmlEnumeration parseEnumerationStructure(EnumDeclaration declaration, UmlNamespace namespace) {
            return new UmlEnumeration(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(), resolveModifiers(declaration), List.of(), List.of(), List.of(), Optional.empty(), Optional.empty());
    }

    private UmlRecord parseRecord(RecordDeclaration declaration, UmlNamespace namespace, JavaTypeResolver typeResolver, JavaTypeResolutionContext context) {
            List<UmlProperty> properties = new ArrayList<>();

        for (Parameter component : declaration.getParameters()) {
            properties.add(new UmlProperty(component.getNameAsString(), UmlVisibility.PRIVATE, typeResolver.resolve(component.getType(), context), Set.of(UmlModifier.FINAL), Optional.empty(), Optional.empty()));
        }

        return new UmlRecord(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(), resolveModifiers(declaration), properties, List.of(), Optional.empty(), Optional.empty());
    }

    private UmlRecord parseRecordStructure(RecordDeclaration declaration, UmlNamespace namespace) {
            return new UmlRecord(declaration.getNameAsString(), namespace, resolveVisibility(declaration), List.of(), resolveModifiers(declaration), List.of(), List.of(), Optional.empty(), Optional.empty());
    }

    private List<String> resolveTemplateParameters(ClassOrInterfaceDeclaration declaration) {
            List<String> parameters = new ArrayList<>();

        declaration.getTypeParameters()
                .forEach(parameter -> parameters.add(parameter.getNameAsString()));

        return List.copyOf(parameters);
    }

    private UmlNamespace resolveNamespace(CompilationUnit compilationUnit) {
            if (compilationUnit.getPackageDeclaration().isEmpty()) {
            return UmlNamespace.root();
        }

        String packageName = compilationUnit
                .getPackageDeclaration()
                .get()
                .getNameAsString();

        return UmlNamespace.of(packageName.split("\\."));
    }

    private UmlVisibility resolveVisibility(ClassOrInterfaceDeclaration declaration) {
            if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)) {
            return UmlVisibility.PUBLIC;
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED)) {
            return UmlVisibility.PROTECTED;
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE)) {
            return UmlVisibility.PRIVATE;
        }

        return UmlVisibility.PACKAGE;
    }

    private UmlVisibility resolveVisibility(EnumDeclaration declaration) {
            if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)) {
            return UmlVisibility.PUBLIC;
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED)) {
            return UmlVisibility.PROTECTED;
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE)) {
            return UmlVisibility.PRIVATE;
        }

        return UmlVisibility.PACKAGE;
    }

    private UmlVisibility resolveVisibility(RecordDeclaration declaration) {
            if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)) {
            return UmlVisibility.PUBLIC;
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PROTECTED)) {
            return UmlVisibility.PROTECTED;
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.PRIVATE)) {
            return UmlVisibility.PRIVATE;
        }

        return UmlVisibility.PACKAGE;
    }

    private Set<UmlModifier> resolveModifiers(ClassOrInterfaceDeclaration declaration) {
            Set<UmlModifier> modifiers = new java.util.HashSet<>();

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT)) {
            modifiers.add(UmlModifier.ABSTRACT);
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL)) {
            modifiers.add(UmlModifier.FINAL);
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.STATIC)) {
            modifiers.add(UmlModifier.STATIC);
        }

        return Set.copyOf(modifiers);
    }

    private Set<UmlModifier> resolveModifiers(EnumDeclaration declaration) {
            Set<UmlModifier> modifiers = new java.util.HashSet<>();

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT)) {
            modifiers.add(UmlModifier.ABSTRACT);
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL)) {
            modifiers.add(UmlModifier.FINAL);
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.STATIC)) {
            modifiers.add(UmlModifier.STATIC);
        }

        return Set.copyOf(modifiers);
    }

    private Set<UmlModifier> resolveModifiers(RecordDeclaration declaration) {
            Set<UmlModifier> modifiers = new java.util.HashSet<>();

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.ABSTRACT)) {
            modifiers.add(UmlModifier.ABSTRACT);
        }

        if (declaration.hasModifier(com.github.javaparser.ast.Modifier.Keyword.FINAL)) {
            modifiers.add(UmlModifier.FINAL);
        }

        return Set.copyOf(modifiers);
    }
}
