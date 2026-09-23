package generador.application.project;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class WhiteList {

    private final Set<String> allowedExternalTypes = new LinkedHashSet<>();

    public WhiteList allow(String externalType) {
        if (externalType != null && !externalType.isBlank()) allowedExternalTypes.add(externalType);
        return this;
    }

    public boolean contains(String externalType) { return allowedExternalTypes.contains(externalType); }

    public Set<String> types() { return Collections.unmodifiableSet(allowedExternalTypes); }
}