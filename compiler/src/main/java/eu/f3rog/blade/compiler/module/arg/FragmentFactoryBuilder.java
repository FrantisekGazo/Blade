package eu.f3rog.blade.compiler.module.arg;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.getSuperClass;

/**
 * Class {@link FragmentFactoryBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class FragmentFactoryBuilder extends BaseClassBuilder {

    private static final String METHOD_NAME_NEW = "new%s";

    public FragmentFactoryBuilder() throws ProcessorError {
        super(GCN.FRAGMENT_FACTORY, GPN.BLADE);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        getBuilder().addModifiers(Modifier.FINAL, Modifier.PUBLIC);
    }

    public void addMethodFor(TypeElement typeElement) throws ProcessorError {
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            return;
        }

        TypeElement superClass = typeElement;
        List<VariableElement> args = new ArrayList<>();

        while (superClass != null) {
            HelperClassBuilder helper = ClassManager.getInstance().getHelperIfExists(superClass);
            if (helper == null) break;

            ArgHelperModule helperModule = helper.getModuleIfExists(ArgHelperModule.class);
            if (helperModule == null) break;

            args.addAll(0, helperModule.getArgs());

            superClass = getSuperClass(superClass);
        }

        integrate(ClassName.get(typeElement), args);
    }

    private void integrate(ClassName fragmentClassName, List<VariableElement> allArgs) throws ProcessorError {
        MethodSpec.Builder forMethod = MethodSpec.methodBuilder(getMethodName(METHOD_NAME_NEW, fragmentClassName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentClassName);

        String fragment = "fragment";
        String args = "args";
        forMethod.addStatement("$T $N = new $T()", fragmentClassName, fragment, fragmentClassName)
                .addStatement("$T $N = new $T()", BundleWrapper.class, args, BundleWrapper.class);

        for (int i = 0; i < allArgs.size(); i++) {
            VariableElement arg = allArgs.get(i);
            TypeName typeName = ClassName.get(arg.asType());
            String name = arg.getSimpleName().toString();
            forMethod.addParameter(typeName, name);
            forMethod.addStatement("$N.put($S, $N)", args, ArgHelperModule.getArgId(arg), name);
        }

        forMethod.addStatement("$N.setArguments($N.getBundle())", fragment, args)
                .addStatement("return $N", fragment);

        getBuilder().addMethod(forMethod.build());
    }

    private String getMethodName(String format, ClassName activityName) {
        return String.format(format, activityName.simpleName());
    }

}
