package generador.application.port.out;

import generador.core.domain.model.UmlModel;

/**
 * Gateway for parsing a PlantUML document into a UmlModel using the
 * project's own DSL parser.
 * Implemented by Infrastructure.
 */
public interface PlantUmlParserPort {
    UmlModel parse(String plantUmlSource);
}
