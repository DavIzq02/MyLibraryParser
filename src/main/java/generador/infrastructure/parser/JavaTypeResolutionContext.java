package generador.infrastructure.parser;

import java.util.List;

public record JavaTypeResolutionContext(
        String packageName,
        List<String> explicitImports,
        List<String> wildcardImports,
        List<String> templateParameters) {

    public JavaTypeResolutionContext {
        packageName = packageName == null
                ? ""
                : packageName;

        explicitImports = explicitImports == null
                ? List.of()
                : List.copyOf(explicitImports);

        wildcardImports = wildcardImports == null
                ? List.of()
                : List.copyOf(wildcardImports);

        templateParameters = templateParameters == null
                ? List.of()
                : List.copyOf(templateParameters);
    }

    public JavaTypeResolutionContext withTemplateParameters(List<String> templateParameters) {
        return new JavaTypeResolutionContext(packageName, explicitImports, wildcardImports, templateParameters);
    }
}