package generador.application.port.in;

/**
 * Use case: persist the current diagram layout for a project.
 */
public interface SaveDiagramLayoutUseCase {
    void saveLayout(String projectId);
}
