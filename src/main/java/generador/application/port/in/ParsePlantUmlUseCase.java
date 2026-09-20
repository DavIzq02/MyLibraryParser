package generador.application.port.in;

import generador.core.domain.model.UmlModel;

/**
 * Use case: parse the project's PlantUML document using the project's
 * own DSL parser.
 */
public interface ParsePlantUmlUseCase {
    UmlModel parse(String projectId);
}
