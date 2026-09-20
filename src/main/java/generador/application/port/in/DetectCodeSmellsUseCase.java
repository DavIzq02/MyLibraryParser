package generador.application.port.in;

/**
 * Use case: detect structural problems (Code Smells) on the UML model.
 */
public interface DetectCodeSmellsUseCase {
    void detectCodeSmells(String projectId);
}
