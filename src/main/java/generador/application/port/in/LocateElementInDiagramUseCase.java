package generador.application.port.in;

/**
 * Use case: locate/highlight a selected element inside the rendered
 * diagram.
 */
public interface LocateElementInDiagramUseCase {
    void locateElement(String projectId, String elementId);
}
