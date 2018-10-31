package eu.f3rog.blade.compiler.arg;

import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Arg;
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.builder.annotation.WeaveBuilder;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.module.BundleUtils;
import eu.f3rog.blade.compiler.name.ClassNames;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.addClassAsParameter;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.cannotHaveAnnotation;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.fullName;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.isFragmentSubClass;

/**
 * Class {@link ArgHelperModule}
 *
 * @author FrantisekGazo
 */
public final class ArgHelperModule
        extends BaseHelperModule {

    private static final String METHOD_NAME_INJECT = "inject";

    public static final String ARG_ID_FORMAT = "<Arg-%s>";

    private final List<BundleUtils.BundledField> mArgs = new ArrayList<>();

    @Override
    public void checkClass(final TypeElement e) throws ProcessorError {
        if (!isFragmentSubClass(e)) {
            throw new ProcessorError(e, ArgErrorMsg.Invalid_class_with_Arg);
        }
    }

    @Override
    public void add(final VariableElement e) throws ProcessorError {
        if (cannotHaveAnnotation(e)) {
            throw new ProcessorError(e, ErrorMsg.Invalid_field_with_annotation, Arg.class.getSimpleName());
        }

        BundleUtils.addBundledField(mArgs, e, Arg.class, new ProcessorUtils.IGetter<Arg, Class<?>>() {
            @Override
            public Class<?> get(Arg a) {
                return a.value();
            }
        });
    }

    @Override
    public boolean implement(final HelperClassBuilder builder) throws ProcessorError {
        addMethodToFragmentFactory(builder);
        if (!mArgs.isEmpty()) {
            // add inject() only if there is something
            addInjectMethod(builder);
            return true;
        }
        return false;
    }

    private void addInjectMethod(final BaseClassBuilder builder) {
        final String target = "target";
        final MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_INJECT)
                .addAnnotation(WeaveBuilder.weave().method("onCreate", ClassNames.Bundle.get())
                        .withStatement("%s.%s(this);", fullName(builder.getClassName()), METHOD_NAME_INJECT)
                        .build())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        addClassAsParameter(method, builder.getArgClassName(), target);

        method.beginControlFlow("if ($N.getArguments() == null)", target)
                .addStatement("return")
                .endControlFlow();

        final String args = "args";
        method.addStatement("$T $N = $T.from($N.getArguments())", BundleWrapper.class, args, BundleWrapper.class, target);

        BundleUtils.getFromBundle(method, target, mArgs, ARG_ID_FORMAT, args);

        builder.getBuilder().addMethod(method.build());
    }

    private void addMethodToFragmentFactory(final HelperClassBuilder builder) throws ProcessorError {
        ClassManager.getInstance()
                .getSpecialClass(FragmentFactoryBuilder.class)
                .addMethodFor(builder.getTypeElement());
    }

}
