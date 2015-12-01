package eu.f3rog.automat.compiler.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import eu.f3rog.automat.compiler.name.GCN;
import eu.f3rog.automat.compiler.name.GPN;
import eu.f3rog.automat.compiler.util.ProcessorError;
import eu.f3rog.automat.compiler.util.ProcessorUtils;
import eu.f3rog.automat.core.BundleWrapper;

import static eu.f3rog.automat.compiler.util.ProcessorUtils.isSubClassOf;

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

    public void integrate(Map<ClassName, FragmentInjectorBuilder> fibs) throws ProcessorError {
        List<VariableElement> args = new ArrayList<>();
        Set<ClassName> classes = fibs.keySet();
        for (Map.Entry<ClassName, FragmentInjectorBuilder> entry : fibs.entrySet()) {
            TypeElement fragmentClass = (TypeElement) entry.getValue().getArgs().get(0).getEnclosingElement();
            if (fragmentClass.getModifiers().contains(Modifier.ABSTRACT)) continue;
            for (ClassName c : classes) {
                if (isSubClassOf(fragmentClass, c)) {
                    args.addAll(fibs.get(c).getArgs());
                }
            }
            args.addAll(entry.getValue().getArgs());
            integrate(entry.getValue(), args);
            args.clear();
        }
    }

    private void integrate(FragmentInjectorBuilder fib, List<VariableElement> allArgs) throws ProcessorError {
        ClassName fragmentClassName = fib.getArgClassName();

        MethodSpec.Builder forMethod = MethodSpec.methodBuilder(getMethodName(METHOD_NAME_NEW, fragmentClassName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentClassName);

        String fragment = "fragment";
        String args = "args";
        forMethod.addStatement("$T $N = new $T()", fragmentClassName, fragment, fragmentClassName)
                .addStatement("$T $N = new $T()", BundleWrapper.class, args, BundleWrapper.class);

        for (VariableElement arg : allArgs) {
            TypeName typeName = ClassName.get(arg.asType());
            String name = arg.getSimpleName().toString();
            forMethod.addParameter(typeName, name);
            forMethod.addStatement("$N.put($S, $N)", args, fib.getExtraId(arg), name);
        }

        forMethod.addStatement("$N.setArguments($N.getBundle())", fragment, args)
                .addStatement("return $N", fragment);

        getBuilder().addMethod(forMethod.build());
    }

    private String getMethodName(String format, ClassName activityName) {
        return String.format(format, activityName.simpleName());
    }

}
