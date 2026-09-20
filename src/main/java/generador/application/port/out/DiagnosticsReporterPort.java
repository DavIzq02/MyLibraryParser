package generador.application.port.out;

/**
 * Gateway for reporting diagnostics about the processing of a project.
 * Implemented by Infrastructure.
 */
public interface DiagnosticsReporterPort {
    void report(String projectId);
}
