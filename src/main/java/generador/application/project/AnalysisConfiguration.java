package generador.application.project;

import java.util.List;

public record AnalysisConfiguration( JavaLanguageVersion javaLanguageVersion, List<String> javaPackageWhitelist, List<String> projectPackageBlacklist ) {
            public AnalysisConfiguration {
        javaPackageWhitelist = javaPackageWhitelist == null
                                ? List.of()
                                : List.copyOf(javaPackageWhitelist);

        projectPackageBlacklist =  projectPackageBlacklist == null
                                ? List.of()
                                : List.copyOf(projectPackageBlacklist);
    }
}