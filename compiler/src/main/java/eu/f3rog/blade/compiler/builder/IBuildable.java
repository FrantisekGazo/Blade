package eu.f3rog.blade.compiler.builder;

import java.io.IOException;

import javax.annotation.processing.Filer;

import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Interface {@link IBuildable}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public interface IBuildable {

    /**
     * Create java class file.
     */
    void build(Filer filer) throws ProcessorError, IOException;

}
