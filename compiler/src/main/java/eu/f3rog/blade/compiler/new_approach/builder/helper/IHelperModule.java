package eu.f3rog.blade.compiler.new_approach.builder.helper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Interface {@link IHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public interface IHelperModule {

    /**
     * Checks if given {@link TypeElement} can have this implementation.
     */
    void checkClass(TypeElement e) throws ProcessorError;

    void add(TypeElement e) throws ProcessorError;

    void add(VariableElement e) throws ProcessorError;

    void add(ExecutableElement e) throws ProcessorError;

    /**
     * Implements into given builder.
     */
    void implement(HelperClassBuilder builder) throws ProcessorError;

}

