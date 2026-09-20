package generador.application.port.in;

/**
 * Use case: export the rendered diagram.
 */
public interface ExportDiagramUseCase {
    void exportDiagram(String projectId, String targetFormat, String destinationPath);
}
