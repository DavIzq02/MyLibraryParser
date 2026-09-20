package generador.core.domain.element;

import java.util.Optional;

public abstract class UmlElement {
            private final String name;
    private final UmlNamespace namespace;
    private final String qualifiedName;
    private final Optional<String> stereotype;
    private final Optional<String> note;

    protected UmlElement(String name, UmlNamespace namespace, Optional<String> stereotype, Optional<String> note) {
        this.name = name;
        this.namespace = namespace;
        this.stereotype = stereotype;
        this.note = note;
        this.qualifiedName = namespace != null ? namespace.qualifiedNameOf(name) : name;
    }

    public String name() {
        return name;
    }

    public UmlNamespace namespace() {
        return namespace;
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    public Optional<String> stereotype() {
        return stereotype;
    }

    public Optional<String> note() {
        return note;
    }
}