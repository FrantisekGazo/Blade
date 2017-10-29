package eu.f3rog.blade.compiler;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Interface {@link ProcessorModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public interface ProcessorModule {

    void prepare() throws ProcessorError;

    void process(TypeElement bladeElement) throws ProcessorError;

    void process(RoundEnvironment roundEnv) throws ProcessorError;

}

