package generador.application.project;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record AnalysisConfiguration(
        JavaLanguageVersion javaLanguageVersion, List<String> javaPackageWhitelist,
        List<String> projectPackageBlacklist, Set<String> excludedPackages,
        Set<String> excludedClasses, WhiteList whiteList, BlackList blackList) {

    public AnalysisConfiguration {
        javaPackageWhitelist = javaPackageWhitelist == null ? List.of() : List.copyOf(javaPackageWhitelist);
        projectPackageBlacklist = projectPackageBlacklist == null ? List.of() : List.copyOf(projectPackageBlacklist);
        excludedPackages = excludedPackages == null ? Set.of() : Set.copyOf(excludedPackages);
        excludedClasses = excludedClasses == null ? Set.of() : Set.copyOf(excludedClasses);
        whiteList = whiteList == null ? new WhiteList() : whiteList;
        blackList = blackList == null ? new BlackList() : blackList;
    }

    public AnalysisConfiguration(JavaLanguageVersion javaLanguageVersion, List<String> javaPackageWhitelist,
            List<String> projectPackageBlacklist) {
        this(javaLanguageVersion, javaPackageWhitelist, projectPackageBlacklist,
                Set.of(), Set.of(), new WhiteList(), new BlackList());
    }

    public static Builder builder(JavaLanguageVersion javaLanguageVersion) { return new Builder(javaLanguageVersion); }

    public static class Builder {
        private final JavaLanguageVersion javaLanguageVersion;
        private final List<String> javaPackageWhitelist = new ArrayList<>();
        private final List<String> projectPackageBlacklist = new ArrayList<>();
        private final Set<String> excludedPackages = new LinkedHashSet<>();
        private final Set<String> excludedClasses = new LinkedHashSet<>();
        private WhiteList whiteList = new WhiteList();
        private BlackList blackList = new BlackList();

        public Builder(JavaLanguageVersion javaLanguageVersion) {
            if (javaLanguageVersion == null) throw new IllegalArgumentException("Java language version cannot be null.");
            this.javaLanguageVersion = javaLanguageVersion;
        }

        public Builder includeJavaPackage(String typeName) {
            if (typeName != null && !typeName.isBlank()) javaPackageWhitelist.add(typeName);
            return this;
        }

        public Builder excludePackage(String packageName) {
            if (packageName != null && !packageName.isBlank()) {
                excludedPackages.add(packageName);
                projectPackageBlacklist.add(packageName);
            }
            return this;
        }

        public Builder excludeClass(String className) {
            if (className != null && !className.isBlank()) excludedClasses.add(className);
            return this;
        }

        public Builder whiteList(WhiteList whiteList) {
            if (whiteList != null) this.whiteList = whiteList;
            return this;
        }

        public Builder blackList(BlackList blackList) {
            if (blackList != null) this.blackList = blackList;
            return this;
        }

        public AnalysisConfiguration build() {
            return new AnalysisConfiguration(javaLanguageVersion, javaPackageWhitelist,
                    projectPackageBlacklist, excludedPackages, excludedClasses, whiteList, blackList);
        }
    }
}