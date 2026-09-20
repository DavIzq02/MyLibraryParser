package generador.core.domain.relationship;

import generador.core.domain.classifier.UmlClassifier;

import java.util.Optional;

public final class UmlRealization implements UmlRelationship {
            private final UmlClassifier implementing;
    private final UmlClassifier contract;

    public UmlRealization( UmlClassifier implementing, UmlClassifier contract, Optional<String> stereotype, Optional<String> note) {
            this.implementing = implementing;
        this.contract = contract;
    }

    public UmlClassifier implementing() {
        return implementing;
    }

    public UmlClassifier contract() {
        return contract;
    }

    @Override
    public UmlClassifier source() {
        return implementing;
    }

    @Override
    public UmlClassifier target() {
        return contract;
    }
}