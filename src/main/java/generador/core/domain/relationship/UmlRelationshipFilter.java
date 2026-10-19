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

/**
 * Regla de dominio para resolver y filtrar relaciones UML entre clasificadores.
 *
 * Cuando dos clasificadores poseen multiples relaciones entre si (por ejemplo,
 * anidamiento y dependencia, o asociacion y dependencia), esta clase garantiza
 * que solo permanezca la relacion mas fuerte de acuerdo a la jerarquia
 * estandar de acoplamiento de UML:
 *
 * 1. Nesting (anidamiento lexico de clase interna / elemento anidado)
 * 2. Generalization (herencia extends)
 * 3. Realization (realizacion de interfaz implements)
 * 4. Association Composite (composicion)
 * 5. Association Shared (agregacion)
 * 6. Association None (asociacion simple por atributo)
 * 7. Dependency (dependencia efimera de uso por parametro o retorno)
 */
public final class UmlRelationshipFilter {

    private UmlRelationshipFilter() {
    }

    /**
     * Filtra la coleccion de relaciones para conservar exactamente una sola
     * relacion (la de mayor fuerza) por cada par no ordenado de clasificadores {A,
     * B},
     * y en caso de coexistir relaciones de jerarquia/contrato (extends e
     * implements)
     * para una misma clase origen, conserva unicamente la de mayor fuerza
     * (UmlRealization).
     */
    public static List<UmlRelationship> filterStrongest(Collection<UmlRelationship> relationships) {
        if (relationships == null || relationships.isEmpty()) {
            return List.of();
        }

        // Paso 0: Identificar interfaces y clasificadores anidados por cada contenedor.
        // Regla: Si una clase contenedora "A" tiene una relación de anidamiento con una interfaz "C"
        // y otra relación de anidamiento con una clase/clasificador "B", si "B" tiene relación con "C",
        // esta no debe dibujarse en el diagrama UML.
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

        // Propagar transitivamente para anidamientos multinivel
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

        // Paso 1: Filtrar relaciones redundantes entre clases anidadas e interfaces anidadas del mismo contenedor
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

        // Paso 2: Si una clase posee conexión de línea sólida (Generalization, Association)
        // Y conexión de línea punteada (Realization, Dependency), solo debe conservarse
        // la relación más fuerte entre ellas.
        Map<String, Boolean> hasSolidBySource = new LinkedHashMap<>();
        Map<String, Boolean> hasDottedBySource = new LinkedHashMap<>();
        Map<String, Integer> maxWeightBySource = new LinkedHashMap<>();

        for (UmlRelationship rel : strongestByPair.values()) {
            if (rel instanceof UmlNesting) {
                continue;
            }

            String sourceKey = rel.source() != null ? rel.source().qualifiedName() : "";
            if (isSolid(rel)) {
                hasSolidBySource.put(sourceKey, true);
            }
            if (isDotted(rel)) {
                hasDottedBySource.put(sourceKey, true);
            }

            int weight = getStrengthWeight(rel);
            maxWeightBySource.merge(sourceKey, weight, Math::max);
        }

        List<UmlRelationship> result = new ArrayList<>();
        for (UmlRelationship rel : strongestByPair.values()) {
            if (rel instanceof UmlNesting) {
                result.add(rel);
                continue;
            }

            String sourceKey = rel.source() != null ? rel.source().qualifiedName() : "";
            boolean hasBothSolidAndDotted = hasSolidBySource.getOrDefault(sourceKey, false)
                    && hasDottedBySource.getOrDefault(sourceKey, false);

            if (hasBothSolidAndDotted) {
                int weight = getStrengthWeight(rel);
                int maxWeight = maxWeightBySource.getOrDefault(sourceKey, 0);
                if (weight >= maxWeight) {
                    result.add(rel);
                }
            } else {
                result.add(rel);
            }
        }

        return List.copyOf(result);
    }

    public static boolean isSolid(UmlRelationship rel) {
        return rel instanceof UmlGeneralization || rel instanceof UmlAssociation;
    }

    public static boolean isDotted(UmlRelationship rel) {
        return rel instanceof UmlRealization || rel instanceof UmlDependency;
    }

    /**
     * Genera una clave canonica simetrica para el par no ordenado {source, target}.
     * A <--> B genera la misma clave que B <--> A.
     */
    private static String buildPairKey(UmlRelationship rel) {
        String s = rel.source() != null ? rel.source().qualifiedName() : "";
        String t = rel.target() != null ? rel.target().qualifiedName() : "";
        return s.compareTo(t) <= 0 ? s + "<-->" + t : t + "<-->" + s;
    }

    /**
     * Asigna un peso ordinal a la relacion de acuerdo a su nivel de fuerza en UML.
     */
    private static int getStrengthWeight(UmlRelationship rel) {
        if (rel instanceof UmlNesting) {
            return 50; // Acoplamiento lexico maximo (inner/nested type)
        }
        if (rel instanceof UmlRealization) {
            return 40; // Implementacion de interfaz (implements) - mayor fuerza contractual
        }
        if (rel instanceof UmlGeneralization) {
            return 35; // Herencia pura (extends)
        }
        if (rel instanceof UmlAssociation assoc) {
            AggregationKind kind = assoc.aggregationKind();
            if (kind == AggregationKind.COMPOSITE) {
                return 23; // Composicion
            } else if (kind == AggregationKind.SHARED) {
                return 22; // Agregacion
            } else {
                return 21; // Asociacion simple
            }
        }
        if (rel instanceof UmlDependency) {
            return 10; // Dependencia de uso
        }
        return 0;
    }

    /**
     * Determina si una relación conecta a una clase/clasificador y una interfaz
     * que son ambas anidadas dentro de un mismo contenedor.
     * En tal caso, la relación entre ellas no debe dibujarse en el diagrama.
     */
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

            // Caso 1: s es clasificador anidado en container y t es interfaz anidada en container
            if (allNested.contains(s) && interfaces.contains(t)) {
                return true;
            }
            // Caso 2: t es clasificador anidado en container y s es interfaz anidada en container
            if (allNested.contains(t) && interfaces.contains(s)) {
                return true;
            }
        }

        return false;
    }
}
