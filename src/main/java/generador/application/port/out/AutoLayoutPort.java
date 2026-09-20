package generador.application.port.out;

import generador.application.project.DiagramLayout;
import generador.core.domain.model.UmlModel;

/**
 * Gateway that computes a DiagramLayout automatically when no saved
 * layout exists for a project.
 * Implemented by Infrastructure.
 */
public interface AutoLayoutPort {
    DiagramLayout computeLayout(UmlModel model);
}
