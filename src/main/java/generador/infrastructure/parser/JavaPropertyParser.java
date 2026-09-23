package generador.infrastructure.parser;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.spec.AggregationKind;
import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;
import generador.core.domain.type.UmlType;
import generador.core.domain.classifier.UmlEnumeration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JavaPropertyParser {

    public List<UmlProperty> parse(FieldDeclaration declaration, JavaTypeResolver typeResolver,
            JavaTypeResolutionContext context, Set<String> constructorParameters) {

        List<UmlProperty> properties = new ArrayList<>();

        for (VariableDeclarator variable : declaration.getVariables()) {
            UmlType resolvedType = resolveType(variable, typeResolver, context);
            properties.add(new UmlProperty(variable.getNameAsString(), resolveVisibility(declaration), resolvedType,
                    resolveModifiers(declaration), variable.getInitializer().map(Object::toString),
                    resolveAggregationKind(variable, constructorParameters, resolvedType)));
        }

        return List.copyOf(properties);
    }

    private UmlVisibility resolveVisibility(FieldDeclaration declaration) {
        if (declaration.hasModifier(Modifier.Keyword.PUBLIC)) return UmlVisibility.PUBLIC;
        if (declaration.hasModifier(Modifier.Keyword.PROTECTED)) return UmlVisibility.PROTECTED;
        if (declaration.hasModifier(Modifier.Keyword.PRIVATE)) return UmlVisibility.PRIVATE;
        return UmlVisibility.PACKAGE;
    }

    private Set<UmlModifier> resolveModifiers(FieldDeclaration declaration) {
        Set<UmlModifier> modifiers = new HashSet<>();
        if (declaration.hasModifier(Modifier.Keyword.STATIC)) modifiers.add(UmlModifier.STATIC);
        if (declaration.hasModifier(Modifier.Keyword.FINAL)) modifiers.add(UmlModifier.FINAL);
        return Set.copyOf(modifiers);
    }

    private UmlType resolveType(VariableDeclarator variable, JavaTypeResolver typeResolver,
            JavaTypeResolutionContext context) {
        return typeResolver.resolve(variable.getType(), context);
    }

    private java.util.Optional<AggregationKind> resolveAggregationKind(VariableDeclarator variable,
            Set<String> constructorParameters, UmlType resolvedType) {

        /*
         * COMPOSITE:
         * The attribute creates its own object.
         */
        if (variable.getInitializer().isPresent() && variable.getInitializer().get() instanceof ObjectCreationExpr)
            return java.util.Optional.of(AggregationKind.COMPOSITE);

        /*
         * SHARED:
         * The attribute is a reference to another
         * classifier of the analyzed project and its
         * name is received through a constructor.
         */
        if (resolvedType instanceof UmlType.Reference reference
                && !(reference.classifier() instanceof UmlEnumeration)
                && constructorParameters.contains(variable.getNameAsString()))
            return java.util.Optional.of(AggregationKind.SHARED);

        return java.util.Optional.empty();
    }
}