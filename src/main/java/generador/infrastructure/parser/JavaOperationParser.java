package generador.infrastructure.parser;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlParameter;
import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;
import generador.core.domain.type.UmlType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JavaOperationParser {
            public List<UmlOperation> parse(MethodDeclaration declaration,JavaTypeResolver typeResolver,JavaTypeResolutionContext context) {
            List<UmlParameter> parameters = new ArrayList<>();

        for (Parameter parameter : declaration.getParameters()) {
            parameters.add(new UmlParameter(parameter.getNameAsString(),resolveType(parameter.getType(), typeResolver,context)));
        }

        return List.of(new UmlOperation(declaration.getNameAsString(), resolveVisibility(declaration), resolveType(declaration.getType(), typeResolver,context), parameters, resolveModifiers(declaration), java.util.Optional.empty()));
    }

    private UmlVisibility resolveVisibility( MethodDeclaration declaration) {
            if (declaration.hasModifier(Modifier.Keyword.PUBLIC)) {
            return UmlVisibility.PUBLIC;
        }

        if (declaration.hasModifier(Modifier.Keyword.PROTECTED)) {
            return UmlVisibility.PROTECTED;
        }

        if (declaration.hasModifier(Modifier.Keyword.PRIVATE)) {
            return UmlVisibility.PRIVATE;
        }

        if (declaration.findAncestor(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::isInterface)
                .orElse(false)) {
            return UmlVisibility.PUBLIC;
        }

        return UmlVisibility.PACKAGE;
    }
    private Set<UmlModifier> resolveModifiers(MethodDeclaration declaration) {
            Set<UmlModifier> modifiers = new HashSet<>();

        if (declaration.hasModifier(Modifier.Keyword.STATIC)) {
            modifiers.add(UmlModifier.STATIC);
        }

        if (declaration.hasModifier(Modifier.Keyword.ABSTRACT)) {
            modifiers.add(UmlModifier.ABSTRACT);
        }

        if (declaration.hasModifier(Modifier.Keyword.FINAL)) {
            modifiers.add(UmlModifier.FINAL);
        }

        if (declaration.hasModifier(Modifier.Keyword.DEFAULT)) {
            modifiers.add(UmlModifier.DEFAULT);
        }

        return Set.copyOf(modifiers);
    }

    private UmlType resolveType(Type type,JavaTypeResolver typeResolver,JavaTypeResolutionContext context) {
        return typeResolver.resolve(type, context);
    }
}