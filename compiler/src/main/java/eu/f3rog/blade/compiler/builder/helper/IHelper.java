package eu.f3rog.blade.compiler.builder.helper;

import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Interface {@link IHelper}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public interface IHelper {

    /**
     * Returns Implementation class for given class.
     * Creates new instance If does not contain yet.
     */
    <T extends IHelperModule> T getModule(Class<T> cls) throws ProcessorError;

    <T extends IHelperModule> T getModuleIfExists(Class<T> cls) throws ProcessorError;

}

