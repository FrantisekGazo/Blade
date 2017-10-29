package eu.f3rog.blade.compiler;


import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link BaseProcessorModule}
 *
 * @author FrantisekGazo
 * @version 2017-10-29
 */
public abstract class BaseProcessorModule
        implements ProcessorModule {

    @Override
    public void prepare() throws ProcessorError {
    }
}

