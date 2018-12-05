package eu.f3rog.blade.compiler.extra;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Extra;
import eu.f3rog.blade.compiler.ErrorMsg;
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
import static eu.f3rog.blade.compiler.util.ProcessorUtils.isActivitySubClass;

/**
 * Class {@link ExtraHelperModule}
 *
 * @author FrantisekGazo
 */
public final class ExtraHelperModule
        extends BaseHelperModule {

    private enum Injected {
        ACTIVITY, SERVICE, INTENT_SERVICE
    }

    private static final String METHOD_NAME_INJECT = "inject";

    public static final String EXTRA_ID_FORMAT = "<Extra-%s>";

    private List<BundleUtils.BundledField> mExtras = new ArrayList<>();
    private Injected mInjected;

    @Override
    public void checkClass(final TypeElement e) throws ProcessorError {
        if (isActivitySubClass(e)) {
            mInjected = Injected.ACTIVITY;
        } else if (ProcessorUtils.isSubClassOf(e, ClassNames.IntentService.get())) {
            mInjected = Injected.INTENT_SERVICE;
        } else if (ProcessorUtils.isSubClassOf(e, ClassNames.Service.get())) {
            mInjected = Injected.SERVICE;
        } else {
            throw new ProcessorError(e, ExtraErrorMsg.Invalid_class_with_Extra);
        }
    }

    @Override
    public void add(final VariableElement e) throws ProcessorError {
        if (cannotHaveAnnotation(e)) {
            throw new ProcessorError(e, ErrorMsg.Invalid_field_with_annotation, Extra.class.getSimpleName());
        }

        BundleUtils.addBundledField(mExtras, e, Extra.class, new ProcessorUtils.IGetter<Extra, Class<?>>() {
            @Override
            public Class<?> get(Extra a) {
                return a.value();
            }
        });
    }

    @Override
    public boolean implement(final HelperClassBuilder builder) throws ProcessorError {
        addMethodToIntentManager(builder);
        if (!mExtras.isEmpty()) {
            // add inject() only if there is something
            addInjectMethod(builder);
            return true;
        }
        return false;
    }

    private void addInjectMethod(final HelperClassBuilder builder) {
        final String target = "target";
        final String intent = "intent";
        final MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_INJECT)
                .addAnnotation(weaveAnnotation(builder))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        addClassAsParameter(method, builder.getArgClassName(), target);

        method.addParameter(ClassNames.Intent.get(), intent);

        method.beginControlFlow("if ($N == null || $N.getExtras() == null)", intent, intent)
                .addStatement("return")
                .endControlFlow();

        final String extras = "extras";
        method.addStatement("$T $N = $T.from($N.getExtras())", BundleWrapper.class, extras, BundleWrapper.class, intent);

        BundleUtils.getFromBundle(method, target, mExtras, EXTRA_ID_FORMAT, extras);

        builder.getBuilder().addMethod(method.build());
    }

    private AnnotationSpec weaveAnnotation(final HelperClassBuilder builder) {
        final String fullClassName = fullName(builder.getClassName());
        switch (mInjected) {
            case ACTIVITY:
                return WeaveBuilder.weave()
                        .method("onCreate", ClassNames.Bundle.get())
                        .withStatement("%s.%s(this, this.getIntent());", fullClassName, METHOD_NAME_INJECT)
                        .and()
                        .method("onNewIntent", ClassNames.Intent.get())
                        .withStatement("%s.%s(this, $1);", fullClassName, METHOD_NAME_INJECT)
                        .build();
            case SERVICE:
                return WeaveBuilder.weave().method("onStartCommand", ClassNames.Intent.get(), TypeName.INT, TypeName.INT)
                        .withStatement("%s.%s(this, $1);", fullClassName, METHOD_NAME_INJECT)
                        .build();
            case INTENT_SERVICE:
                return WeaveBuilder.weave().method("onHandleIntent", ClassNames.Intent.get())
                        .withStatement("%s.%s(this, $1);", fullClassName, METHOD_NAME_INJECT)
                        .build();
            default:
                throw new IllegalStateException();
        }
    }

    private void addMethodToIntentManager(final HelperClassBuilder builder) throws ProcessorError {
        ClassManager.getInstance()
                .getSpecialClass(IntentBuilderBuilder.class)
                .addMethodsFor(builder.getTypeElement());
    }
}
