package eu.f3rog.automat.compiler.builder;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import eu.f3rog.automat.compiler.name.GCN;
import eu.f3rog.automat.compiler.util.ProcessorError;

/**
 * Class {@link BaseInjectorBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public abstract class BaseInjectorBuilder extends BaseClassBuilder {

    protected static final String METHOD_NAME_INJECT = "inject";
    protected static final String EXTRA_ID_FORMAT = "<Extra-%s>";


    public BaseInjectorBuilder(GCN genClassName, ClassName arg) throws ProcessorError {
        super(genClassName, arg);
    }

    @Override
    protected String getPackage() {
        return getArgClassName().packageName();
    }

    @Override
    public void start() throws ProcessorError {
        super.start();

        getBuilder().addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    @Override
    public void end() throws ProcessorError {
        super.end();

        addInjectMethod();
    }

    protected abstract void addInjectMethod();

    public String getExtraId(VariableElement extra) {
        return String.format(EXTRA_ID_FORMAT, extra.getSimpleName().toString());
    }

}
