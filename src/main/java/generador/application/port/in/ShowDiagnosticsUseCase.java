package generador.application.port.in;

/**
 * Use case: show diagnostics about the processing of a project
 * (parsing, model building, layout, export).
 */
public interface ShowDiagnosticsUseCase {
    void showDiagnostics(String projectId);
}
