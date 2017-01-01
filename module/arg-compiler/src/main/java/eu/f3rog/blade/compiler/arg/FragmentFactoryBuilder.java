package eu.f3rog.blade.compiler.arg;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Arg;
import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.module.BundleUtils;
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
 */
public final class FragmentFactoryBuilder
        extends BaseClassBuilder {

    private static final String METHOD_NAME_NEW = "new%s";

    public FragmentFactoryBuilder() throws ProcessorError {
        super(GCN.FRAGMENT_FACTORY, GPN.BLADE);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        getBuilder().addModifiers(Modifier.PUBLIC);
    }

    public void addMethodFor(final TypeElement typeElement) throws ProcessorError {
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

    private void integrate(final ClassName fragmentClassName,
                           final List<VariableElement> fields) throws ProcessorError {
        final MethodSpec.Builder forMethodBuilder = MethodSpec.methodBuilder(getMethodName(METHOD_NAME_NEW, fragmentClassName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(fragmentClassName);

        final String fragment = "fragment";
        final String args = "args";

        forMethodBuilder.addStatement("$T $N = new $T()",
                fragmentClassName, fragment, fragmentClassName);
        forMethodBuilder.addStatement("$T $N = new $T()",
                BundleWrapper.class, args, BundleWrapper.class);

        final ProcessorUtils.IGetter<Arg, Class<?>> classGetter = new ProcessorUtils.IGetter<Arg, Class<?>>() {
            @Override
            public Class<?> get(final Arg a) {
                return a.value();
            }
        };

        for (int i = 0, c = fields.size(); i < c; i++) {
            final VariableElement field = fields.get(i);
            final Type type = ProcessorUtils.getBoundedType(field);
            final TypeName typeName = ClassName.get(type);
            final String name = field.getSimpleName().toString();

            forMethodBuilder.addParameter(typeName, name);

            final BundleUtils.BundledField bundledField = BundleUtils.getBundledField(field, Arg.class, classGetter);
            BundleUtils.putToBundle(forMethodBuilder, null, bundledField, ArgHelperModule.ARG_ID_FORMAT, args);
        }

        forMethodBuilder.addStatement("$N.setArguments($N.getBundle())",
                fragment, args);
        forMethodBuilder.addStatement("return $N",
                fragment);

        getBuilder().addMethod(forMethodBuilder.build());
    }

    private String getMethodName(final String format, final ClassName fragmentClassName) {
        return String.format(format, NameUtils.getNestedName(fragmentClassName, ""));
    }
}
