package generador.application.port.in;

/**
 * Use case: move an element visually within the diagram.
 */
public interface MoveDiagramElementUseCase {
    void moveElement(String projectId, String elementId, double x, double y);
}
