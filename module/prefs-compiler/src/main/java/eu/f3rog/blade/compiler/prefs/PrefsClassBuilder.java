package eu.f3rog.blade.compiler.prefs;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Modifier;

import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link PrefsClassBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-10-21
 */
public final class PrefsClassBuilder
        extends BaseClassBuilder {

    public PrefsClassBuilder(ClassName className) throws ProcessorError {
        super(new GCN("%s_Prefs"), className);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        getBuilder().addModifiers(Modifier.PUBLIC);
    }
}
