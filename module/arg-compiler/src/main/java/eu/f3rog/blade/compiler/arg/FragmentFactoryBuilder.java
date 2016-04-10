package eu.f3rog.blade.compiler.arg;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Arg;
import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.name.NameUtils;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.core.BundleWrapper;

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
        getBuilder().addModifiers(Modifier.PUBLIC);
    }

    public void addMethodFor(TypeElement typeElement) throws ProcessorError {
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            return;
        }

        List<VariableElement> args = new ArrayList<>();

        List<? extends Element> elements = ProcessorUtils.getElementUtils().getAllMembers(typeElement);
        for (Element e : elements) {
            if (e instanceof VariableElement && e.getAnnotation(Arg.class) != null) {
                args.add((VariableElement) e);
            }
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
            forMethod.addStatement("$N.put($S, $N)", args, ArgHelperModule.getArgId(name), name);
        }

        forMethod.addStatement("$N.setArguments($N.getBundle())", fragment, args)
                .addStatement("return $N", fragment);

        getBuilder().addMethod(forMethod.build());
    }

    private String getMethodName(String format, ClassName fragmentClassName) {
        return String.format(format, NameUtils.getNestedName(fragmentClassName, ""));
    }
}
