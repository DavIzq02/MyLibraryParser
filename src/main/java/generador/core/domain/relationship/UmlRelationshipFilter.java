package generador.core.domain.relationship;

import generador.core.domain.spec.AggregationKind;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private UmlRelationshipFilter() {}

    /**
     * Filtra la coleccion de relaciones para conservar exactamente una sola
     * relacion (la de mayor fuerza) por cada par no ordenado de clasificadores {A, B}.
     */
    public static List<UmlRelationship> filterStrongest(Collection<UmlRelationship> relationships) {
        if (relationships == null || relationships.isEmpty()) {
            return List.of();
        }

        Map<String, UmlRelationship> strongestByPair = new LinkedHashMap<>();

        for (UmlRelationship rel : relationships) {
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
        if (rel instanceof UmlGeneralization) {
            return 40; // Herencia pura (extends)
        }
        if (rel instanceof UmlRealization) {
            return 30; // Implementacion de interfaz (implements)
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
}
