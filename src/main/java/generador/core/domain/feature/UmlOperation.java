package generador.core.domain.feature;

import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;
import generador.core.domain.type.UmlType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record UmlOperation( String name, UmlVisibility visibility, UmlType returnType, List<UmlParameter> parameters, Set<UmlModifier> modifiers, Optional<String> stereotype ) {
            public UmlOperation {
        parameters = parameters == null ? List.of() : List.copyOf(parameters);
        modifiers = modifiers == null ? Set.of() : Set.copyOf(modifiers);
        stereotype = stereotype == null ? Optional.empty() : stereotype;
    }

    public boolean isLeaf() {
        return modifiers.contains(UmlModifier.LEAF);
    }

    public boolean isStatic() {
        return modifiers.contains(UmlModifier.STATIC);
    }

    public boolean isAbstract() {
        return modifiers.contains(UmlModifier.ABSTRACT);
    }

    public boolean isDefault() {
        return modifiers.contains(UmlModifier.DEFAULT);
    }
}