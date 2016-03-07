package eu.f3rog.blade.compiler.builder.helper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link BaseHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public abstract class BaseHelperModule implements IHelperModule {

    @Override
    public void add(TypeElement e) throws ProcessorError {
        throw new ProcessorError(e, ErrorMsg.Invalid_place);
    }

    @Override
    public void add(VariableElement e) throws ProcessorError {
        throw new ProcessorError(e, ErrorMsg.Invalid_place);
    }

    @Override
    public void add(ExecutableElement e) throws ProcessorError {
        throw new ProcessorError(e, ErrorMsg.Invalid_place);
    }

}
