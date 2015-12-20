package eu.f3rog.blade.compiler.module.state;

import android.os.Bundle;

import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.State;
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.module.BundleUtil;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.cannotHaveAnnotation;

/**
 * Class {@link StateHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class StateHelperModule
        extends BaseHelperModule {

    private static final String METHOD_NAME_SAVE_SATE = "saveState";
    private static final String METHOD_NAME_RESTORE_SATE = "restoreState";

    private static final String STATEFUL_ID_FORMAT = "<Stateful-%s>";

    private final List<VariableElement> mStatefulFields = new ArrayList<>();

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        // support any class
    }

    @Override
    public void add(VariableElement e) throws ProcessorError {
        if (cannotHaveAnnotation(e)) {
            throw new ProcessorError(e, ErrorMsg.Invalid_field_with_annotation, State.class.getSimpleName());
        }

        mStatefulFields.add(e);
    }

    @Override
    public void implement(HelperClassBuilder builder) throws ProcessorError {
        addSaveStateMethod(builder);
        addRestoreStateMethod(builder);
    }

    private void addSaveStateMethod(HelperClassBuilder builder) {
        String target = "target";
        String state = "state";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_SAVE_SATE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target)
                .addParameter(Bundle.class, state);

        String bundleWrapper = "bundleWrapper";
        method.beginControlFlow("if ($N == null)", state)
                .addStatement("throw new $T($S)", IllegalArgumentException.class, "State cannot be null!")
                .endControlFlow()
                .addStatement("$T $N = $T.from($N)", BundleWrapper.class, bundleWrapper, BundleWrapper.class, state);

        BundleUtil.putToBundle(method, target, mStatefulFields, STATEFUL_ID_FORMAT, bundleWrapper);

        builder.getBuilder().addMethod(method.build());
    }

    private void addRestoreStateMethod(HelperClassBuilder builder) {
        String target = "target";
        String state = "state";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_RESTORE_SATE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target)
                .addParameter(Bundle.class, state);

        String bundleWrapper = "bundleWrapper";
        method.beginControlFlow("if ($N == null)", state)
                .addStatement("return")
                .endControlFlow()
                .addStatement("$T $N = $T.from($N)", BundleWrapper.class, bundleWrapper, BundleWrapper.class, state);

        BundleUtil.getFromBundle(method, target, mStatefulFields, STATEFUL_ID_FORMAT, bundleWrapper);

        builder.getBuilder().addMethod(method.build());
    }

}
