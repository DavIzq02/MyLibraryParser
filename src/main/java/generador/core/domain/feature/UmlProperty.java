package generador.core.domain.feature;

import generador.core.domain.spec.AggregationKind;
import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;
import generador.core.domain.type.UmlType;

import java.util.Optional;
import java.util.Set;

public record UmlProperty( String name, UmlVisibility visibility, UmlType type, Set<UmlModifier> modifiers, Optional<String> initialValue, Optional<AggregationKind> aggregationKind ) {
            public UmlProperty {
        modifiers = modifiers == null ? Set.of() : Set.copyOf(modifiers);
        initialValue = initialValue == null ? Optional.empty() : initialValue;
        aggregationKind = aggregationKind == null ? Optional.empty() : aggregationKind;
    }

    public boolean isStatic() {
        return modifiers.contains(UmlModifier.STATIC);
    }

    public boolean isReadOnly() {
        return modifiers.contains(UmlModifier.READ_ONLY);
    }

    public boolean isFinal() {
        return modifiers.contains(UmlModifier.FINAL);
    }
}