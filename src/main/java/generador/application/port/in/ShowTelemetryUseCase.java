package generador.application.port.in;

/**
 * Use case: show detailed telemetry collected while processing a
 * project.
 */
public interface ShowTelemetryUseCase {
    void showTelemetry(String projectId);
}
