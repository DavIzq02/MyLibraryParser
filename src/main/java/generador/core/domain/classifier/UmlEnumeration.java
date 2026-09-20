package generador.core.domain.classifier;

import generador.core.domain.element.UmlNamespace;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class UmlEnumeration extends UmlClassifier {
            private final List<UmlEnumerationLiteral> literals;

    public UmlEnumeration( String name, UmlNamespace namespace, UmlVisibility visibility, List<String> templateParameters, Set<UmlModifier> modifiers, List<UmlProperty> properties, List<UmlOperation> operations, List<UmlEnumerationLiteral> literals, Optional<String> stereotype, Optional<String> note) {
            super(name, namespace, visibility, templateParameters, modifiers, properties, operations, stereotype, note);

        this.literals = literals == null
                ? List.of()
                : List.copyOf(literals);
    }

    public List<UmlEnumerationLiteral> literals() {
        return literals;
    }
}