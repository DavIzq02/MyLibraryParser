package generador.core.domain.model;

import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.relationship.UmlRelationship;

import java.util.Map;
import java.util.Set;
import java.util.Optional;

public record UmlModel( Map<String, UmlClassifier> classifiers, Set<UmlRelationship> relationships ) {
            // Búsqueda del clasificador por su qualifiedName en el mapa
    public Optional<UmlClassifier> findClassifier(String qualifiedName) {
        if (classifiers == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(classifiers.get(qualifiedName));
    }
}