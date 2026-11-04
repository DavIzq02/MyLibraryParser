package generador.core.domain.relationship;

import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.classifier.UmlInterface;
import generador.core.domain.spec.AggregationKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UmlRelationshipFilter {

    private UmlRelationshipFilter() {
    }

    public static List<UmlRelationship> filterStrongest(Collection<UmlRelationship> relationships) {
        if (relationships == null || relationships.isEmpty()) {
            return List.of();
        }

        Map<String, Set<String>> nestedInterfacesByContainer = new LinkedHashMap<>();
        Map<String, Set<String>> nestedClassifiersByContainer = new LinkedHashMap<>();

        for (UmlRelationship rel : relationships) {
            if (rel instanceof UmlNesting nesting) {
                String container = nesting.outerClassifier() != null ? nesting.outerClassifier().qualifiedName() : "";
                UmlClassifier inner = nesting.innerClassifier();
                String innerName = inner != null ? inner.qualifiedName() : "";

                if (!container.isEmpty() && !innerName.isEmpty()) {
                    nestedClassifiersByContainer.computeIfAbsent(container, k -> new LinkedHashSet<>()).add(innerName);
                    if (inner instanceof UmlInterface) {
                        nestedInterfacesByContainer.computeIfAbsent(container, k -> new LinkedHashSet<>()).add(innerName);
                    }
                }
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, Set<String>> entry : nestedClassifiersByContainer.entrySet()) {
                Set<String> currentNested = new LinkedHashSet<>(entry.getValue());
                for (String child : currentNested) {
                    Set<String> grandChildren = nestedClassifiersByContainer.get(child);
                    if (grandChildren != null && entry.getValue().addAll(grandChildren)) {
                        changed = true;
                    }
                    Set<String> grandChildInterfaces = nestedInterfacesByContainer.get(child);
                    if (grandChildInterfaces != null) {
                        Set<String> targetSet = nestedInterfacesByContainer.computeIfAbsent(entry.getKey(), k -> new LinkedHashSet<>());
                        if (targetSet.addAll(grandChildInterfaces)) {
                            changed = true;
                        }
                    }
                }
            }
        }

        List<UmlRelationship> candidateRelationships = new ArrayList<>();
        for (UmlRelationship rel : relationships) {
            if (!isRedundantSiblingInterfaceRelationship(rel, nestedInterfacesByContainer, nestedClassifiersByContainer)) {
                candidateRelationships.add(rel);
            }
        }

        Map<String, UmlRelationship> strongestByPair = new LinkedHashMap<>();

        for (UmlRelationship rel : candidateRelationships) {
            String key = buildPairKey(rel);
            UmlRelationship current = strongestByPair.get(key);

            if (current == null) {
                strongestByPair.put(key, rel);
            } else {
                int currentWeight = getStrengthWeight(current);
                int newWeight = getStrengthWeight(rel);

                if (newWeight > currentWeight) {
                    strongestByPair.put(key, rel);
                }
            }
        }

        return List.copyOf(strongestByPair.values());
    }

    public static boolean isSolid(UmlRelationship rel) {
        return rel instanceof UmlGeneralization || rel instanceof UmlAssociation;
    }

    public static boolean isDotted(UmlRelationship rel) {
        return rel instanceof UmlRealization || rel instanceof UmlDependency;
    }

    private static String buildPairKey(UmlRelationship rel) {
        String s = rel.source() != null ? rel.source().qualifiedName() : "";
        String t = rel.target() != null ? rel.target().qualifiedName() : "";
        return s.compareTo(t) <= 0 ? s + "<-->" + t : t + "<-->" + s;
    }

    private static int getStrengthWeight(UmlRelationship rel) {
        if (rel instanceof UmlNesting) {
            return 50; 
        }
        if (rel instanceof UmlRealization) {
            return 40; 
        }
        if (rel instanceof UmlGeneralization) {
            return 35; 
        }
        if (rel instanceof UmlAssociation assoc) {
            AggregationKind kind = assoc.aggregationKind();
            if (kind == AggregationKind.COMPOSITE) {
                return 23; 
            } else if (kind == AggregationKind.SHARED) {
                return 22; 
            } else {
                return 21; 
            }
        }
        if (rel instanceof UmlDependency) {
            return 10; 
        }
        return 0;
    }

    private static boolean isRedundantSiblingInterfaceRelationship(
            UmlRelationship rel,
            Map<String, Set<String>> nestedInterfacesByContainer,
            Map<String, Set<String>> nestedClassifiersByContainer) {

        if (rel instanceof UmlNesting) {
            return false;
        }

        String s = rel.source() != null ? rel.source().qualifiedName() : "";
        String t = rel.target() != null ? rel.target().qualifiedName() : "";

        if (s.isEmpty() || t.isEmpty() || s.equals(t)) {
            return false;
        }

        for (Map.Entry<String, Set<String>> entry : nestedInterfacesByContainer.entrySet()) {
            String container = entry.getKey();
            Set<String> interfaces = entry.getValue();
            Set<String> allNested = nestedClassifiersByContainer.getOrDefault(container, Set.of());

            if (allNested.contains(s) && interfaces.contains(t)) {
                return true;
            }
            if (allNested.contains(t) && interfaces.contains(s)) {
                return true;
            }
        }

        return false;
    }
}