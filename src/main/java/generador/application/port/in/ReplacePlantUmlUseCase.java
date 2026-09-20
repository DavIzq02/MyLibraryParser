package generador.application.port.in;

import generador.application.project.PlantUmlDocument;

/**
 * Use case: replace the PlantUML document currently associated with a
 * project.
 */
public interface ReplacePlantUmlUseCase {
    PlantUmlDocument replacePlantUml(String projectId, String filePath);
}
