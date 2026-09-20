package generador.application.port.in;

import generador.core.domain.model.UmlModel;

/**
 * Use case: build the in-memory UML model and identify all supported
 * UML elements, exposing them as a navigable tree.
 */
public interface BuildUmlModelUseCase {
    UmlModel buildModel(String projectId);
}
