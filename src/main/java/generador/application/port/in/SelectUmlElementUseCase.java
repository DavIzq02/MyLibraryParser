package generador.application.port.in;

/**
 * Use case: select an element from the navigable UML tree.
 */
public interface SelectUmlElementUseCase {
    void selectElement(String projectId, String elementId);
}
