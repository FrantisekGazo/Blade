package eu.f3rog.blade.compiler.prefs;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

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

    public PrefsClassBuilder(TypeElement typeElement) throws ProcessorError {
        super(new GCN("%s_Prefs"), ClassName.get(typeElement));

        if (typeElement.getKind() != ElementKind.INTERFACE) {
            throw new ProcessorError(typeElement, PrefsErrorMsg.Invalid_type_with_Prefs);
        }
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        getBuilder().addModifiers(Modifier.PUBLIC);
    }
}
