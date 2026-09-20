package generador.core.domain.classifier;

import generador.core.domain.element.UmlNamespace;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class UmlRecord extends UmlClassifier {
            public UmlRecord( String name, UmlNamespace namespace, UmlVisibility visibility, List<String> templateParameters, Set<UmlModifier> modifiers, List<UmlProperty> properties, List<UmlOperation> operations, Optional<String> stereotype, Optional<String> note) {
            super(name, namespace, visibility, templateParameters, modifiers, properties, operations, stereotype, note);
    }
}