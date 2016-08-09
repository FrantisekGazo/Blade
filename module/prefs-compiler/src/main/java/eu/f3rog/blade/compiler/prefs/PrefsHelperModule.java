package eu.f3rog.blade.compiler.prefs;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link PrefsHelperModule}
 *
 * @author FrantisekGazo
 * @version 2016-08-09
 */
public final class PrefsHelperModule
        extends BaseHelperModule {

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        if (e.getKind() != ElementKind.INTERFACE) {
            throw new ProcessorError(e, PrefsErrorMsg.Invalid_type_with_Prefs);
        }
    }

    @Override
    public void add(TypeElement e) throws ProcessorError {
    }

    @Override
    public boolean implement(HelperClassBuilder builder) throws ProcessorError {
        return false;
    }
}
