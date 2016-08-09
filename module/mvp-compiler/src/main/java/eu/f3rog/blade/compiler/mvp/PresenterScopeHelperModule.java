package eu.f3rog.blade.compiler.mvp;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.mvp.MvpActivity;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.isActivitySubClass;

/**
 * Class {@link PresenterScopeHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public final class PresenterScopeHelperModule
        extends BaseHelperModule {

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        if (!isActivitySubClass(e)) {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean implement(HelperClassBuilder builder) throws ProcessorError {
        // add interface
        builder.getBuilder().addSuperinterface(ClassName.get(MvpActivity.class));

        return true;
    }

}
