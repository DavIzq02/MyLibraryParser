package generador.core.domain.classifier;

import generador.core.domain.element.UmlElement;
import generador.core.domain.element.UmlNamespace;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.spec.UmlModifier;
import generador.core.domain.spec.UmlVisibility;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class UmlClassifier extends UmlElement {
            private final UmlVisibility visibility;
    private final List<String> templateParameters;
    private final Set<UmlModifier> modifiers;
    private final List<UmlProperty> properties;
    private final List<UmlOperation> operations;

    protected UmlClassifier( String name, UmlNamespace namespace, UmlVisibility visibility, List<String> templateParameters, Set<UmlModifier> modifiers, List<UmlProperty> properties, List<UmlOperation> operations, Optional<String> stereotype, Optional<String> note) {
            super(name, namespace, stereotype, note);

        this.visibility = visibility;
        this.templateParameters = templateParameters;
        this.modifiers = modifiers;
        this.properties = properties;
        this.operations = operations;
    }

    public UmlVisibility visibility() {
        return visibility;
    }

    public List<String> templateParameters() {
        return templateParameters;
    }

    public Set<UmlModifier> modifiers() {
        return modifiers;
    }

    public List<UmlProperty> properties() {
        return properties;
    }

    public List<UmlOperation> operations() {
        return operations;
    }

    public boolean isAbstract() {
        return modifiers.contains(UmlModifier.ABSTRACT);
    }

    public boolean isLeaf() {
        return modifiers.contains(UmlModifier.LEAF);
    }

    public boolean isStatic() {
        return modifiers.contains(UmlModifier.STATIC);
    }
}