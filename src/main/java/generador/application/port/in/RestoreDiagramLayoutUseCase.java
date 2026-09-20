package generador.application.port.in;

/**
 * Use case: restore a project's saved layout when it is reopened, * falling back to AutoLayout when no layout exists.
 */
public interface RestoreDiagramLayoutUseCase {
    void restoreLayout(String projectId);
}
