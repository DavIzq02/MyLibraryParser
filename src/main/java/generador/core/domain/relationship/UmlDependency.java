package generador.core.domain.relationship;


import generador.core.domain.classifier.UmlClassifier;

import java.util.Optional;

public final class UmlDependency implements UmlRelationship {
            private final UmlClassifier client;
    private final UmlClassifier supplier;

    // Constructor público («Create»)
    public UmlDependency( UmlClassifier client, UmlClassifier supplier, Optional<String> stereotype, Optional<String> note) {
            this.client = client;
        this.supplier = supplier;
    }

    // Métodos consultores (Getters)
    public UmlClassifier client() {
        return client;
    }

    public UmlClassifier supplier() {
        return supplier;
    }

    // Métodos de origen y destino de la relación
    public UmlClassifier source() {
        return client;
    }

    public UmlClassifier target() {
        return supplier;
    }

    @Override
    public String toString() {
        return "UmlDependency{" +
                "client=" + client +
                ", supplier=" + supplier +
                '}';
    }
}