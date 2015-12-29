package eu.f3rog.blade.compiler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Interface {@link ProcessorModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public interface ProcessorModule {

    void process(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnv) throws ProcessorError;

}

