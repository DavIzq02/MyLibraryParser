package generador.application.project;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record AnalysisConfiguration(
        JavaLanguageVersion javaLanguageVersion, List<String> javaPackageWhitelist,
        List<String> projectPackageBlacklist, List<String> projectClassBlacklist,
        Set<String> excludedPackages, Set<String> excludedClasses,
        WhiteList whiteList, BlackList blackList) {

    public AnalysisConfiguration {
        javaPackageWhitelist = javaPackageWhitelist == null ? List.of() : List.copyOf(javaPackageWhitelist);
        projectPackageBlacklist = projectPackageBlacklist == null ? List.of() : List.copyOf(projectPackageBlacklist);
        projectClassBlacklist = projectClassBlacklist == null ? List.of() : List.copyOf(projectClassBlacklist);

        Set<String> allExcludedPackages = new LinkedHashSet<>();
        if (projectPackageBlacklist != null) {
            for (String p : projectPackageBlacklist) {
                if (p != null && !p.isBlank()) allExcludedPackages.add(p.trim());
            }
        }
        if (excludedPackages != null) {
            for (String p : excludedPackages) {
                if (p != null && !p.isBlank()) allExcludedPackages.add(p.trim());
            }
        }
        excludedPackages = Set.copyOf(allExcludedPackages);

        Set<String> allExcludedClasses = new LinkedHashSet<>();
        if (projectClassBlacklist != null) {
            for (String c : projectClassBlacklist) {
                if (c != null && !c.isBlank()) allExcludedClasses.add(c.trim());
            }
        }
        if (excludedClasses != null) {
            for (String c : excludedClasses) {
                if (c != null && !c.isBlank()) allExcludedClasses.add(c.trim());
            }
        }
        excludedClasses = Set.copyOf(allExcludedClasses);

        whiteList = whiteList == null ? new WhiteList() : whiteList;
        blackList = blackList == null ? new BlackList() : blackList;
    }

    public AnalysisConfiguration(JavaLanguageVersion javaLanguageVersion, List<String> javaPackageWhitelist,
            List<String> projectPackageBlacklist) {
        this(javaLanguageVersion, javaPackageWhitelist, projectPackageBlacklist,
                List.of(), Set.of(), Set.of(), new WhiteList(), new BlackList());
    }

    public AnalysisConfiguration(JavaLanguageVersion javaLanguageVersion, List<String> javaPackageWhitelist,
            List<String> projectPackageBlacklist, List<String> projectClassBlacklist) {
        this(javaLanguageVersion, javaPackageWhitelist, projectPackageBlacklist,
                projectClassBlacklist, Set.of(), Set.of(), new WhiteList(), new BlackList());
    }

    public static Builder builder(JavaLanguageVersion javaLanguageVersion) { return new Builder(javaLanguageVersion); }

    public static class Builder {
        private final JavaLanguageVersion javaLanguageVersion;
        private final List<String> javaPackageWhitelist = new ArrayList<>();
        private final List<String> projectPackageBlacklist = new ArrayList<>();
        private final List<String> projectClassBlacklist = new ArrayList<>();
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
                excludedPackages.add(packageName.trim());
                projectPackageBlacklist.add(packageName.trim());
            }
            return this;
        }

        public Builder excludePackages(List<String> packageNames) {
            if (packageNames != null) {
                for (String packageName : packageNames) {
                    excludePackage(packageName);
                }
            }
            return this;
        }

        public Builder excludeClass(String className) {
            if (className != null && !className.isBlank()) {
                excludedClasses.add(className.trim());
                projectClassBlacklist.add(className.trim());
            }
            return this;
        }

        public Builder excludeClasses(List<String> classNames) {
            if (classNames != null) {
                for (String className : classNames) {
                    excludeClass(className);
                }
            }
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
                    projectPackageBlacklist, projectClassBlacklist, excludedPackages, excludedClasses, whiteList, blackList);
        }
    }
}