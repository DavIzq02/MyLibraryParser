package generador.core.domain.relationship;

import generador.core.domain.classifier.UmlClassifier;

public interface UmlRelationship {
            UmlClassifier source();

    UmlClassifier target();
}