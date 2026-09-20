package generador.application.port.in;

import generador.application.project.Project;

/**
 * Use case: open an existing project.
 */
public interface OpenProjectUseCase {
    Project openProject(String projectId);
}
