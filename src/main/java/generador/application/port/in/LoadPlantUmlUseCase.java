package generador.application.port.in;

import generador.application.project.PlantUmlDocument;

/**
 * Use case: load a PlantUML (.puml) file into a project.
 */
public interface LoadPlantUmlUseCase {
    PlantUmlDocument loadPlantUml(String projectId, String filePath);
}
