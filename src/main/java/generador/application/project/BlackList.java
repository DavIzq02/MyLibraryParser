package generador.application.project;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class BlackList {

    private final Set<String> excludedExternalTypes = new LinkedHashSet<>();

    public BlackList exclude(String externalType) {
        if (externalType != null && !externalType.isBlank()) excludedExternalTypes.add(externalType);
        return this;
    }

    public boolean contains(String externalType) { return excludedExternalTypes.contains(externalType); }

    public Set<String> types() { return Collections.unmodifiableSet(excludedExternalTypes); }
}