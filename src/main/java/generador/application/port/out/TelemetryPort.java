package generador.application.port.out;

/**
 * Gateway for recording and exposing detailed telemetry about the
 * processing of a project.
 * Implemented by Infrastructure.
 */
public interface TelemetryPort {
    void record(String projectId, String eventName);
}
