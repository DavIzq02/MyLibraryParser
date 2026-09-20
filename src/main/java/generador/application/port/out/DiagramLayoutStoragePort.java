package generador.application.port.out;

import generador.application.project.DiagramLayout;

/**
 * Gateway for saving and restoring a project's diagram layout, * independently from the PlantUML document.
 * Implemented by Infrastructure.
 */
public interface DiagramLayoutStoragePort {
    void save(String projectId, DiagramLayout layout);

    DiagramLayout load(String projectId);
}
