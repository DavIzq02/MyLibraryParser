package generador.application.port.out;

import generador.application.project.Project;

/**
 * Gateway for creating, reading and deleting projects.
 * Implemented by Infrastructure (e.g. file system persistence).
 */
public interface ProjectRepositoryPort {
    void save(Project project);

    Project findById(String projectId);

    void delete(String projectId);
}
