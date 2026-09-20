package generador.application.port.out;

import generador.core.domain.model.UmlModel;

/**
 * Gateway for detecting structural problems (Code Smells) exclusively
 * over the UML model, never over Java source code.
 * Implemented by Infrastructure.
 */
public interface CodeSmellDetectorPort {
    void detect(UmlModel model);
}
