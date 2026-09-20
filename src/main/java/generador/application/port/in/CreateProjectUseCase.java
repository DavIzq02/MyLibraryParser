package generador.application.port.in;

import generador.application.project.Project;

/**
 * Use case: create a new project.
 */
public interface CreateProjectUseCase {
    Project createProject(String projectName);
}
