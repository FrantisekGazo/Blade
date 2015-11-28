package eu.f3rog.automat.compiler.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import eu.f3rog.automat.compiler.name.GCN;
import eu.f3rog.automat.compiler.util.ProcessorError;
import eu.f3rog.automat.compiler.util.StringUtils;

/**
 * Class {@link BaseInjectorBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public abstract class BaseInjectorBuilder extends BaseClassBuilder {

    protected static final String METHOD_NAME_INJECT = "inject";
    protected static final String EXTRA_ID_FORMAT = "%s.%s-%s";


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

    /**
     * Returns getter for given Extra type.
     */
    protected String getExtraGetterName(TypeName typeName) {
        if (typeName.isPrimitive()) {
            return String.format("get%s", StringUtils.startUpperCase(typeName.toString()));
        } else if (typeName.equals(ClassName.get(String.class))) {
            return "getString";
        } else {
            return "get";
        }
    }

    /**
     * Returns setter for given Arg type.
     */
    protected String getArgSetterName(TypeName typeName) {
        if (typeName.isPrimitive()) {
            return String.format("put%s", StringUtils.startUpperCase(typeName.toString()));
        } else if (typeName.equals(ClassName.get(String.class))) {
            return "putString";
        } else {
            return "putSerializable";
        } // TODO : putParcelable, putStringArray, putXArray, ...
    }

    public String getExtraId(VariableElement extra) {
        return String.format(EXTRA_ID_FORMAT,
                getArgClassName().packageName(), getArgClassName().simpleName(), extra.getSimpleName().toString());
    }

}
