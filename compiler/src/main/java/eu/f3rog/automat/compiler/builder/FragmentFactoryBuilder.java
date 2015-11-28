package eu.f3rog.automat.compiler.builder;

import android.os.Bundle;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import eu.f3rog.automat.compiler.name.GCN;
import eu.f3rog.automat.compiler.name.GPN;
import eu.f3rog.automat.compiler.util.ProcessorError;

/**
 * Class {@link FragmentFactoryBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-10-21
 */
public class FragmentFactoryBuilder extends BaseClassBuilder {

    private static final String METHOD_NAME_NEW = "new%s";

    public FragmentFactoryBuilder() throws ProcessorError {
        super(GCN.FRAGMENT_FACTORY, GPN.AUTOMAT);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        getBuilder().addModifiers(Modifier.FINAL, Modifier.PUBLIC);
    }

    public void integrate(FragmentInjectorBuilder fib) throws ProcessorError {
        ClassName fragmentClassName = fib.getArgClassName();

        MethodSpec.Builder forMethod = MethodSpec.methodBuilder(getMethodName(METHOD_NAME_NEW, fragmentClassName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentClassName);

        String fragment = "fragment";
        String args = "args";
        forMethod.addStatement("$T $N = new $T()", fragmentClassName, fragment, fragmentClassName)
                .addStatement("$T $N = new $T()", Bundle.class, args, Bundle.class);

        for (VariableElement arg : fib.getArgs()) {
            TypeName typeName = ClassName.get(arg.asType());
            String name = arg.getSimpleName().toString();
            forMethod.addParameter(typeName, name);
            forMethod.addStatement("$N.$N($S, $N)", args, fib.getArgSetterName(typeName), fib.getExtraId(arg), name);
        }

        forMethod.addStatement("$N.setArguments($N)", fragment, args)
                .addStatement("return $N", fragment);

        getBuilder().addMethod(forMethod.build());
    }

    private String getMethodName(String format, ClassName activityName) {
        return String.format(format, activityName.simpleName());
    }

}
