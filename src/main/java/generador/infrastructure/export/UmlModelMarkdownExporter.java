package generador.infrastructure.export;

import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlParameter;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.model.UmlModel;
import generador.core.domain.relationship.UmlRelationship;
import generador.application.project.AnalysisConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UmlModelMarkdownExporter {
            public void export( UmlModel model, AnalysisConfiguration configuration, Path outputPath) {
            String content = buildContent(model, configuration);

        try {
            Files.writeString(outputPath, content);
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible escribir el archivo de salida: "
                            + outputPath, exception);
        }
    }
    private String buildContent( UmlModel model, AnalysisConfiguration configuration) {
            StringBuilder output = new StringBuilder();

        appendHeader(output);

        appendSummary(output, model, configuration);

        appendClassifiers(output, model);

        appendRelationships(output, model);

        return output.toString();
    }
    private void appendHeader( StringBuilder output) {
            output.append("# UML Model\n\n");

        output.append("> Modelo UML generado automáticamente "
                        + "a partir del análisis de un proyecto Java.\n\n");

        output.append("---\n\n");
    }

    private void appendSummary( StringBuilder output, UmlModel model, AnalysisConfiguration configuration) {
            output.append("## 1. Summary\n\n");

        output.append("| Element | Quantity |\n");
        output.append("|---|---:|\n");

        output.append("| Classifiers | ")
                .append(model.classifiers().size())
                .append(" |\n");

        output.append("| Relationships | ")
                .append(model.relationships().size())
                .append(" |\n\n");

        output.append("### Analyzed classes\n\n");

        for (UmlClassifier classifier :
                model.classifiers().values()) {
            output.append("- `")
                    .append(classifier.qualifiedName())
                    .append("`\n");
        }

        output.append("\n");

        output.append("### Java package whitelist\n\n");

        if (configuration.javaPackageWhitelist().isEmpty()) {
            output.append("_None._\n\n");

        } else {
            for (String packageName :
                    configuration.javaPackageWhitelist()) {
            output.append("- `")
                        .append(packageName)
                        .append("`\n");
            }

            output.append("\n");
        }

        output.append("### Project package blacklist\n\n");

        if (configuration.projectPackageBlacklist().isEmpty()) {
            output.append("_None._\n\n");

        } else {
            for (String packageName :
                    configuration.projectPackageBlacklist()) {
            output.append("- `")
                        .append(packageName)
                        .append("`\n");
            }

            output.append("\n");
        }
    }
    private void appendClassifiers( StringBuilder output, UmlModel model) {
            output.append("## 2. Classifiers\n\n");

        for (UmlClassifier classifier :
                model.classifiers().values()) {
            appendClassifier(output, classifier);
        }
    }

    private void appendClassifier( StringBuilder output, UmlClassifier classifier) {
            output.append("### `")
                .append(classifier.qualifiedName())
                .append("`\n\n");

        output.append("**Type:** `")
                .append(classifier.getClass().getSimpleName())
                .append("`\n\n");

        output.append("**Visibility:** `")
                .append(classifier.visibility())
                .append("`\n\n");

        if (!classifier.templateParameters().isEmpty()) {
            output.append("**Template parameters:** `")
                    .append(String.join(", ", classifier.templateParameters()))
                    .append("`\n\n");
        }

        if (!classifier.modifiers().isEmpty()) {
            output.append("**Modifiers:** `")
                    .append(classifier.modifiers())
                    .append("`\n\n");
        }

        appendProperties(output, classifier);

        appendOperations(output, classifier);

        output.append("---\n\n");
    }

    private void appendProperties( StringBuilder output, UmlClassifier classifier) {
            output.append("#### Properties\n\n");

        if (classifier.properties().isEmpty()) {
            output.append("_None._\n\n");

            return;
        }

        output.append("| Visibility | Name | Type | Resolution | Modifiers |\n");

        output.append("|---|---|---|---|---|\n");

        for (UmlProperty property :
                classifier.properties()) {
            output.append("| `")
                    .append(property.visibility())
                    .append("` | `")
                    .append(property.name())
                    .append("` | `")
                    .append(property.type().name())
                    .append("` | `")
                    .append(property.type().getClass().getSimpleName())
                    .append("` | `")
                    .append(property.modifiers())
                    .append("` |\n");
        }

        output.append("\n");
    }
    private void appendOperations( StringBuilder output, UmlClassifier classifier) {
            output.append("#### Operations\n\n");

        if (classifier.operations().isEmpty()) {
            output.append("_None._\n\n");

            return;
        }

        output.append("| Visibility | Operation | Return | Return Resolution |\n");

        output.append("|---|---|---|---|\n");

        for (UmlOperation operation :
                classifier.operations()) {
            output.append("| `")
                    .append(operation.visibility())
                    .append("` | `")
                    .append(buildOperationSignature(operation))
                    .append("` | `")
                    .append(operation.returnType().name())
                    .append("` | `")
                    .append(operation.returnType().getClass().getSimpleName())
                    .append("` |\n");
        }

        output.append("\n");
    }

    private String buildOperationSignature( UmlOperation operation) {
            StringBuilder signature = new StringBuilder();

        signature.append(operation.name())
                .append("(");

        for (int index = 0;
             index < operation.parameters().size();
             index++) {
            if (index > 0) {
                signature.append(", ");
            }

            UmlParameter parameter = operation.parameters().get(index);

            signature.append(parameter.name())
                    .append(" : ")
                    .append(parameter.type().name());
        }

        signature.append(")");

        return signature.toString();
    }

    private void appendRelationships( StringBuilder output, UmlModel model) {
            output.append("## 3. Relationships\n\n");

        if (model.relationships().isEmpty()) {
            output.append("_None._\n");

            return;
        }

        output.append("| Type | Source | Target |\n");

        output.append("|---|---|---|\n");

        for (UmlRelationship relationship :
                model.relationships()) {
            output.append("| `")
                    .append(relationship
                                    .getClass()
                                    .getSimpleName())
                    .append("` | `")
                    .append(relationship
                                    .source()
                                    .qualifiedName())
                    .append("` | `")
                    .append(relationship
                                    .target()
                                    .qualifiedName())
                    .append("` |\n");
        }

        output.append("\n");
    }
}