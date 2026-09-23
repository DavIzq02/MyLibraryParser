package generador.core.domain.model;

import generador.core.domain.classifier.UmlAnnotation;
import generador.core.domain.classifier.UmlClass;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.classifier.UmlEnumeration;
import generador.core.domain.classifier.UmlInterface;
import generador.core.domain.classifier.UmlRecord;
import generador.core.domain.feature.UmlOperation;
import generador.core.domain.feature.UmlProperty;
import generador.core.domain.relationship.UmlRelationship;
import generador.core.domain.type.UmlType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record UmlModel(Map<String, UmlClassifier> classifiers, Set<UmlRelationship> relationships) {

    public Optional<UmlClassifier> findClassifier(String qualifiedName) {
        if (classifiers == null) return Optional.empty();
        return Optional.ofNullable(classifiers.get(qualifiedName));
    }

    public int totalPackages() {
        if (classifiers == null) return 0;
        return (int) classifiers.values().stream().map(UmlClassifier::qualifiedName)
                .filter(name -> name.contains("::"))
                .map(name -> name.substring(0, name.lastIndexOf("::"))).distinct().count();
    }

    public int totalClassifiers() { return classifiers == null ? 0 : classifiers.size(); }
    public int totalClasses() { return countClassifiers(UmlClass.class); }

    public int totalAbstractClasses() {
        if (classifiers == null) return 0;
        return (int) classifiers.values().stream()
                .filter(classifier -> classifier instanceof UmlClass && classifier.isAbstract()).count();
    }

    public int totalConcreteClasses() {
        if (classifiers == null) return 0;
        return (int) classifiers.values().stream()
                .filter(classifier -> classifier instanceof UmlClass && !classifier.isAbstract()).count();
    }

    public int totalInterfaces() { return countClassifiers(UmlInterface.class); }
    public int totalEnums() { return countClassifiers(UmlEnumeration.class); }
    public int totalRecords() { return countClassifiers(UmlRecord.class); }
    public int totalAnnotations() { return countClassifiers(UmlAnnotation.class); }

    public int totalProperties() {
        if (classifiers == null) return 0;
        return classifiers.values().stream().mapToInt(classifier -> classifier.properties().size()).sum();
    }

    public int totalMethods() {
        if (classifiers == null) return 0;
        return classifiers.values().stream().mapToInt(classifier -> (int) classifier.operations().stream()
                .filter(operation -> !operation.isConstructor()).count()).sum();
    }

    public int totalConstructors() {
        if (classifiers == null) return 0;
        return classifiers.values().stream().mapToInt(classifier -> (int) classifier.operations().stream()
                .filter(UmlOperation::isConstructor).count()).sum();
    }

    public int totalRelationships() { return relationships == null ? 0 : relationships.size(); }

    public int totalExternalTypes() {
        if (classifiers == null) return 0;
        return (int) classifiers.values().stream().flatMap(classifier -> {
            Set<UmlType> types = new java.util.LinkedHashSet<>();

            // 1. Tipos de las propiedades.
            classifier.properties().forEach(property -> types.add(property.type()));

            // 2. Tipos usados en operaciones.
            classifier.operations().forEach(operation -> {
                // Tipo de retorno.
                types.add(operation.returnType());

                // Parámetros.
                operation.parameters().forEach(parameter -> types.add(parameter.type()));
            });

            return types.stream();
        }).flatMap(type -> externalTypes(type).stream()).distinct().count();
    }

    private Set<String> externalTypes(UmlType type) {
        if (type instanceof UmlType.ExternalReference externalReference) {
            return Set.of(externalReference.qualifiedName());
        }

        if (type instanceof UmlType.Array array) return externalTypes(array.elementType());
        if (type instanceof UmlType.Nullable nullable) return externalTypes(nullable.wrapped());

        if (type instanceof UmlType.Parameterized parameterized) {
            Set<String> result = new java.util.LinkedHashSet<>();

            // Tipo base.
            result.addAll(externalTypes(parameterized.base()));

            // Tipos genéricos.
            for (UmlType argument : parameterized.typeArguments()) result.addAll(externalTypes(argument));

            return result;
        }

        return Set.of();
    }

    private int countClassifiers(Class<? extends UmlClassifier> classifierType) {
        if (classifiers == null) return 0;
        return (int) classifiers.values().stream()
                .filter(classifier -> classifierType.isInstance(classifier)).count();
    }
}