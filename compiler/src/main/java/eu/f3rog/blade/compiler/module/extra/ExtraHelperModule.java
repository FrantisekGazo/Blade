package eu.f3rog.blade.compiler.module.extra;

import android.app.Activity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Extra;
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.module.BundleUtils;
import eu.f3rog.blade.compiler.name.EClass;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.module.WeaveUtils.createWeaveAnnotation;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.cannotHaveAnnotation;

/**
 * Class {@link ExtraHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class ExtraHelperModule extends BaseHelperModule {

    private static final String METHOD_NAME_INJECT = "inject";
    private static final String WEAVE_INTO = "onCreate";

    private static final String EXTRA_ID_FORMAT = "<Extra-%s>";

    public static String getExtraId(String extra) {
        return String.format(EXTRA_ID_FORMAT, extra);
    }

    private List<String> mExtras = new ArrayList<>();

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        if (!ProcessorUtils.isSubClassOf(e, EClass.AppCompatActivity.getName(), ClassName.get(Activity.class))) {
            throw new ProcessorError(e, ErrorMsg.Invalid_class_with_Extra);
        }
    }

    @Override
    public void add(VariableElement e) throws ProcessorError {
        if (cannotHaveAnnotation(e)) {
            throw new ProcessorError(e, ErrorMsg.Invalid_field_with_annotation, Extra.class.getSimpleName());
        }

        mExtras.add(e.getSimpleName().toString());
    }

    @Override
    public void implement(ProcessingEnvironment processingEnvironment, HelperClassBuilder builder) throws ProcessorError {
        addInjectMethod(builder);
        addMethodToActivityNavigator(processingEnvironment, builder);
    }

    private void addInjectMethod(HelperClassBuilder builder) {
        String target = "target";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_INJECT)
                .addAnnotation(createWeaveAnnotation(WEAVE_INTO))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target);

        method.beginControlFlow("if ($N.getIntent() == null || $N.getIntent().getExtras() == null)", target, target)
                .addStatement("return")
                .endControlFlow();

        String extras = "extras";
        method.addStatement("$T $N = $T.from($N.getIntent().getExtras())", BundleWrapper.class, extras, BundleWrapper.class, target);

        BundleUtils.getFromBundle(method, target, mExtras, EXTRA_ID_FORMAT, extras);

        builder.getBuilder().addMethod(method.build());
    }

    private void addMethodToActivityNavigator(ProcessingEnvironment processingEnvironment, HelperClassBuilder builder) throws ProcessorError {
        ClassManager.getInstance()
                .getSpecialClass(ActivityNavigatorBuilder.class)
                .addMethodsFor(processingEnvironment, builder.getTypeElement());
    }

    public List<String> getExtras() {
        return mExtras;
    }
}
