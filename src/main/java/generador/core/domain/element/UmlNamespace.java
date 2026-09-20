package generador.core.domain.element;

import java.util.List;
import java.util.ArrayList;

public record UmlNamespace(List<String> segments) {
            public static UmlNamespace root() {
        return new UmlNamespace(List.of());
    }

    public static UmlNamespace of(List<String> segments) {
        return new UmlNamespace(List.copyOf(segments));
    }

    public static UmlNamespace of(String... segments) {
        return new UmlNamespace(List.of(segments));
    }

    public boolean isRoot() {
        return segments == null || segments.isEmpty();
    }

    public UmlNamespace child(String segment) {
        List<String> newSegments = new ArrayList<>(this.segments);
        newSegments.add(segment);
        return new UmlNamespace(List.copyOf(newSegments));
    }

    public String qualifiedNameOf(String elementName) {
        if (isRoot()) {
            return elementName;
        }
        return String.join("::", segments) + "::" + elementName;
    }

    @Override
    public String toString() {
        return String.join("::", segments);
    }
}