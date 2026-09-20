package generador.core.domain.relationship;

import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.spec.AggregationKind;

import java.util.Optional;

public final class UmlAssociation implements UmlRelationship {
            private final UmlClassifier source;
    private final String sourceMultiplicity;
    private final UmlClassifier target;
    private final String targetMultiplicity;
    private final AggregationKind aggregationKind;

    public UmlAssociation( UmlClassifier source, String sourceMultiplicity, UmlClassifier target, String targetMultiplicity, AggregationKind aggregationKind, Optional<String> stereotype, Optional<String> note) {
            this.source = source;
        this.sourceMultiplicity = sourceMultiplicity == null
                        ? ""
                        : sourceMultiplicity;

        this.target = target;
        this.targetMultiplicity = targetMultiplicity == null
                        ? ""
                        : targetMultiplicity;

        this.aggregationKind = aggregationKind == null
                        ? AggregationKind.NONE
                        : aggregationKind;
    }

    @Override
    public UmlClassifier source() {
        return source;
    }

    public String sourceMultiplicity() {
        return sourceMultiplicity;
    }

    @Override
    public UmlClassifier target() {
        return target;
    }

    public String targetMultiplicity() {
        return targetMultiplicity;
    }

    public AggregationKind aggregationKind() {
        return aggregationKind;
    }
}