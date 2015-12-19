package eu.f3rog.blade.compiler.module.extra;

import android.app.Activity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.compiler.name.EClass;
import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.builder.MiddleManBuilder;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.core.BundleWrapper;

/**
 * Class {@link ExtraHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class ExtraHelperModule extends BaseHelperModule {

    private static final String METHOD_NAME_INJECT = "inject";
    private static final String EXTRA_ID_FORMAT = "<Extra-%s>";

    public static String getExtraId(VariableElement extra) {
        return String.format(EXTRA_ID_FORMAT, extra.getSimpleName().toString());
    }

    private List<VariableElement> mExtras = new ArrayList<>();

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        if (!ProcessorUtils.isSubClassOf(e, EClass.AppCompatActivity.getName(), ClassName.get(Activity.class))) {
            throw new ProcessorError(e, ErrorMsg.Invalid_class_with_Extra);
        }
    }

    @Override
    public void add(VariableElement e) throws ProcessorError {
        if (e.getModifiers().contains(Modifier.PRIVATE)
                || e.getModifiers().contains(Modifier.PROTECTED)
                || e.getModifiers().contains(Modifier.FINAL)) {
            throw new ProcessorError(e, ErrorMsg.Invalid_Extra_field);
        }

        mExtras.add(e);
    }

    @Override
    public void implement(HelperClassBuilder builder) throws ProcessorError {
        addInjectMethod(builder);
        addMethodToActivityNavigator(builder);
        addCall(builder);
    }

    private void addInjectMethod(HelperClassBuilder builder) {
        String target = "target";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_INJECT)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target);

        method.beginControlFlow("if ($N.getIntent() == null || $N.getIntent().getExtras() == null)", target, target)
                .addStatement("return")
                .endControlFlow();

        String extras = "extras";
        method.addStatement("$T $N = $T.from($N.getIntent().getExtras())", BundleWrapper.class, extras, BundleWrapper.class, target);

        for (int i = 0; i < mExtras.size(); i++) {
            VariableElement extra = mExtras.get(i);
            method.addStatement("$N.$N = $N.get($S, $N.$N)",
                    target, extra.getSimpleName(),
                    extras, getExtraId(extra),
                    target, extra.getSimpleName());
        }

        builder.getBuilder().addMethod(method.build());
    }

    private void addMethodToActivityNavigator(HelperClassBuilder builder) throws ProcessorError {
        ClassManager.getInstance()
                .getSpecialClass(ActivityNavigatorBuilder.class)
                .addMethodsFor(builder.getTypeElement());
    }

    private void addCall(BaseClassBuilder builder) {
        ClassManager.getInstance()
                .getSpecialClass(MiddleManBuilder.class)
                .addCall(builder, METHOD_NAME_INJECT);
    }

    public List<VariableElement> getExtras() {
        return mExtras;
    }
}
