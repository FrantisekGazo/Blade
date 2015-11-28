package eu.f3rog.automat.compiler.builder;

import android.app.Activity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import eu.f3rog.automat.compiler.ErrorMsg;
import eu.f3rog.automat.compiler.name.EClass;
import eu.f3rog.automat.compiler.name.GCN;
import eu.f3rog.automat.compiler.name.GPN;
import eu.f3rog.automat.compiler.util.ProcessorError;
import eu.f3rog.automat.compiler.util.ProcessorUtils;

/**
 * Class {@link InjectorBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class InjectorBuilder extends BaseClassBuilder {

    private static final String METHOD_NAME_INJECT = "inject";

    private Map<ClassName, ActivityInjectorBuilder> mActivityInjectorBuilders = new HashMap<>();

    public InjectorBuilder() throws ProcessorError {
        super(GCN.INJECTOR, GPN.AUTOMAT);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();

        getBuilder().addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    @Override
    public void end() throws ProcessorError {
        super.end();
    }

    public void addExtra(final VariableElement e) throws ProcessorError {
        if (e.getModifiers().contains(Modifier.PRIVATE) || e.getModifiers().contains(Modifier.PROTECTED)) {
            throw new ProcessorError(e, ErrorMsg.Extra_cannot_be_private_or_protected);
        }
        if (e.getModifiers().contains(Modifier.FINAL)) {
            throw new ProcessorError(e, ErrorMsg.Extra_cannot_be_final);
        }

        TypeElement classElement = (TypeElement) e.getEnclosingElement();
        if (!ProcessorUtils.isSubClassOf(classElement, EClass.AppCompatActivity.getName(), ClassName.get(Activity.class))) {
            throw new ProcessorError(classElement, ErrorMsg.Invalid_class_with_Extra);
        }

        ActivityInjectorBuilder aib = getActivityInjectorBuilder(ClassName.get(classElement));

        aib.addExtra(e);
    }

    private ActivityInjectorBuilder getActivityInjectorBuilder(final ClassName activityClassName) throws ProcessorError {
        ActivityInjectorBuilder builder = mActivityInjectorBuilders.get(activityClassName);
        if (builder == null) {
            builder = new ActivityInjectorBuilder(activityClassName);
            mActivityInjectorBuilders.put(activityClassName, builder);
        }
        return builder;
    }

    private void addInjectMethod(ActivityInjectorBuilder aib) {
        String injector = "injector";
        String target = "target";

        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_INJECT)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(aib.getArgClassName(), target)
                .addStatement("$T $N = new $T()", aib.getClassName(), injector, aib.getClassName())
                .addStatement("$N.$N($N)", injector, METHOD_NAME_INJECT, target);

        getBuilder().addMethod(method.build());
    }

    private String fullNameOf(ClassName className) {
        return className.packageName() + "." + className.simpleName();
    }

    @Override
    public void build(Filer filer) throws ProcessorError, IOException {
        for (Map.Entry<ClassName, ActivityInjectorBuilder> entry : mActivityInjectorBuilders.entrySet()) {
            entry.getValue().build(filer);
            addInjectMethod(entry.getValue());
        }
        super.build(filer);
    }

    public Map<ClassName, ActivityInjectorBuilder> getActivityInjectorBuilders() {
        return mActivityInjectorBuilders;
    }
}
