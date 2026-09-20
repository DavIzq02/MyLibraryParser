package generador.core.domain.type;

import generador.core.domain.classifier.UmlClassifier;

import java.util.List;

public sealed interface UmlType permits UmlType.Array, UmlType.Nullable, UmlType.TemplateParameter, UmlType.Primitive, UmlType.Parameterized, UmlType.Reference, UmlType.ExternalReference, UmlType.Unknown {
            String name();

    record Unknown(String rawRepresentation) implements UmlType {
            @Override
        public String name() {
            return rawRepresentation;
        }
    }

    record Array(UmlType elementType, int dimensions) implements UmlType {
            @Override
        public String name() {
            return elementType.name() + "[]".repeat(dimensions);
        }
    }

    record Nullable(UmlType wrapped) implements UmlType {
            @Override
        public String name() {
            return wrapped.name() + "?";
        }
    }

    record TemplateParameter(String parameterName) implements UmlType {
            @Override
        public String name() {
            return parameterName;
        }
    }

    record Primitive(String name) implements UmlType {
    }

    record ExternalReference(String qualifiedName) implements UmlType {
            @Override
        public String name() {
            return qualifiedName;
        }
    }
    record Parameterized(UmlType base, List<UmlType> typeArguments) implements UmlType {
            @Override
        public String name() {
            return base.name() + "<"
                    + typeArguments.stream()
                    .map(UmlType::name)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("")
                    + ">";
        }
    }

    record Reference(UmlClassifier classifier) implements UmlType {
            @Override
        public String name() {
            return classifier.name();
        }
    }
}