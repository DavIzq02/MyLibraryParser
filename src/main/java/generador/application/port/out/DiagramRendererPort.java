package generador.application.port.out;

import generador.application.project.DiagramLayout;
import generador.core.domain.model.UmlModel;

/**
 * Gateway for rendering a UML model, combined with a layout, into a
 * displayable diagram.
 * Implemented by Infrastructure.
 */
public interface DiagramRendererPort {
    void render(UmlModel model, DiagramLayout layout);
}
