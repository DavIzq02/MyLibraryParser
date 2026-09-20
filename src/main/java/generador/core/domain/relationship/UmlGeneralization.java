package generador.core.domain.relationship;

import generador.core.domain.classifier.UmlClassifier;

import java.util.Optional;

public final class UmlGeneralization implements UmlRelationship {
            private final UmlClassifier specific;
    private final UmlClassifier general;

    public UmlGeneralization( UmlClassifier specific, UmlClassifier general, Optional<String> stereotype, Optional<String> note) {
            this.specific = specific;
        this.general = general;
    }

    public UmlClassifier specific() {
        return specific;
    }

    public UmlClassifier general() {
        return general;
    }

    @Override
    public UmlClassifier source() {
        return specific;
    }

    @Override
    public UmlClassifier target() {
        return general;
    }
}