package eu.f3rog.blade.compiler.module.state;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.State;
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.builder.weaving.WeaveBuilder;
import eu.f3rog.blade.compiler.module.BundleUtils;
import eu.f3rog.blade.compiler.name.EClass;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.cannotHaveAnnotation;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.fullName;

/**
 * Class {@link StateHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class StateHelperModule
        extends BaseHelperModule {

    private enum HelpedClassType {
        ACTIVITY_OR_FRAGMENT, VIEW, OTHER
    }

    private static final String METHOD_NAME_SAVE_SATE = "saveState";
    private static final String METHOD_NAME_RESTORE_SATE = "restoreState";

    private static final String WEAVE_onSaveInstanceState = "onSaveInstanceState";
    private static final String WEAVE_onRestoreInstanceState = "onRestoreInstanceState";
    private static final String WEAVE_onCreate = "onCreate";

    private static final String STATEFUL_ID_FORMAT = "<Stateful-%s>";

    private final List<String> mStatefulFields = new ArrayList<>();
    private HelpedClassType mHelpedClassType;

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        // support any class
        if (ProcessorUtils.isSubClassOf(e, ClassName.get(Fragment.class), EClass.SupportFragment.getName(),
                ClassName.get(Activity.class), EClass.AppCompatActivity.getName())) {
            mHelpedClassType = HelpedClassType.ACTIVITY_OR_FRAGMENT;
        } else if (ProcessorUtils.isSubClassOf(e, View.class)) {
            mHelpedClassType = HelpedClassType.VIEW;
            if (hasViewImplementedStateMethod(e)) {
                throw new ProcessorError(e, ErrorMsg.View_cannot_implement_state_methods);
            }
        } else {
            mHelpedClassType = HelpedClassType.OTHER;
        }
    }

    private boolean hasViewImplementedStateMethod(TypeElement viewType) {
        List<? extends Element> elements = viewType.getEnclosedElements();
        for (Element e : elements) {
            if (e.getKind() == ElementKind.METHOD) {
                String name = e.getSimpleName().toString();
                if (name.equals(WEAVE_onSaveInstanceState) || name.equals(WEAVE_onRestoreInstanceState)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void add(VariableElement e) throws ProcessorError {
        if (cannotHaveAnnotation(e)) {
            throw new ProcessorError(e, ErrorMsg.Invalid_field_with_annotation, State.class.getSimpleName());
        }

        mStatefulFields.add(e.getSimpleName().toString());
    }

    @Override
    public void implement(ProcessingEnvironment processingEnvironment, HelperClassBuilder builder) throws ProcessorError {
        addSaveStateMethod(builder);
        addRestoreStateMethod(builder);
    }

    private void addSaveStateMethod(HelperClassBuilder builder) {
        String target = "target";
        String state = "state";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_SAVE_SATE)
                .addAnnotation(weaveSave(builder.getClassName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target)
                .addParameter(Bundle.class, state);

        String bundleWrapper = "bundleWrapper";
        method.beginControlFlow("if ($N == null)", state)
                .addStatement("throw new $T($S)", IllegalArgumentException.class, "State cannot be null!")
                .endControlFlow()
                .addStatement("$T $N = $T.from($N)", BundleWrapper.class, bundleWrapper, BundleWrapper.class, state);

        BundleUtils.putToBundle(method, target, mStatefulFields, STATEFUL_ID_FORMAT, bundleWrapper);

        builder.getBuilder().addMethod(method.build());
    }

    private void addRestoreStateMethod(HelperClassBuilder builder) {
        String target = "target";
        String state = "state";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_RESTORE_SATE)
                .addAnnotation(weaveRestore(builder.getClassName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target)
                .addParameter(Bundle.class, state);

        String bundleWrapper = "bundleWrapper";
        method.beginControlFlow("if ($N == null)", state)
                .addStatement("return")
                .endControlFlow()
                .addStatement("$T $N = $T.from($N)", BundleWrapper.class, bundleWrapper, BundleWrapper.class, state);

        BundleUtils.getFromBundle(method, target, mStatefulFields, STATEFUL_ID_FORMAT, bundleWrapper);

        builder.getBuilder().addMethod(method.build());
    }

    private AnnotationSpec weaveSave(ClassName helperName) {
        switch (mHelpedClassType) {
            case ACTIVITY_OR_FRAGMENT:
                return WeaveBuilder.into(WEAVE_onSaveInstanceState, Bundle.class)
                        .addStatement("%s.%s(this, $1);", fullName(helperName), METHOD_NAME_SAVE_SATE)
                        .build();
            case VIEW:
                return WeaveBuilder.into(WEAVE_onSaveInstanceState)
                        .addStatement("%s bundle = new %s();", Bundle.class.getName(), Bundle.class.getName())
                        .addStatement("bundle.putParcelable('PARENT_STATE', super.onSaveInstanceState());")
                        .addStatement("%s.%s(this, bundle);", fullName(helperName), METHOD_NAME_SAVE_SATE)
                        .addStatement("return bundle;")
                        .build();
            default:
                throw new IllegalStateException();
        }
    }

    private AnnotationSpec weaveRestore(ClassName helperName) {
        switch (mHelpedClassType) {
            case ACTIVITY_OR_FRAGMENT:
                return WeaveBuilder.into(WEAVE_onCreate, Bundle.class)
                        .addStatement("%s.%s(this, $1);", fullName(helperName), METHOD_NAME_RESTORE_SATE)
                        .build();
            case VIEW:
                return WeaveBuilder.into(WEAVE_onRestoreInstanceState, Parcelable.class)
                        .addStatement("if ($1 instanceof %s) {", Bundle.class.getName())
                        .addStatement("%s bundle = (%s) $1;", Bundle.class.getName(), Bundle.class.getName())
                        .addStatement("%s.%s(this, bundle);", fullName(helperName), METHOD_NAME_RESTORE_SATE)
                        .addStatement("super.onRestoreInstanceState(bundle.getParcelable('PARENT_STATE'));")
                        .addStatement("} else {")
                        .addStatement("super.onRestoreInstanceState($1);")
                        .addStatement("}")
                        .addStatement("return;")
                        .build();
            default:
                throw new IllegalStateException();
        }
    }

}
