package generador.core.domain.relationship;

import generador.core.domain.classifier.UmlClassifier;

import java.util.Optional;

public final class UmlNesting implements UmlRelationship {
            private final UmlClassifier outerClassifier;
    private final UmlClassifier innerClassifier;

    public UmlNesting( UmlClassifier outerClassifier, UmlClassifier innerClassifier, Optional<String> stereotype, Optional<String> note) {
            this.outerClassifier = outerClassifier;
        this.innerClassifier = innerClassifier;
    }

    public UmlClassifier outerClassifier() {
        return outerClassifier;
    }

    public UmlClassifier innerClassifier() {
        return innerClassifier;
    }

    @Override
    public UmlClassifier source() {
        return outerClassifier;
    }

    @Override
    public UmlClassifier target() {
        return innerClassifier;
    }
}