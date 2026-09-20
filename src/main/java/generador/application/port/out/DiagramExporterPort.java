package generador.application.port.out;

/**
 * Gateway for exporting a rendered diagram to an external format/file.
 * Implemented by Infrastructure.
 */
public interface DiagramExporterPort {
    void export(String projectId, String targetFormat, String destinationPath);
}
