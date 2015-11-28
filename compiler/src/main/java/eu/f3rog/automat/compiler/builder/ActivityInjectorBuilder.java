package eu.f3rog.automat.compiler.builder;

import android.app.Activity;
import android.os.Bundle;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import eu.f3rog.automat.compiler.name.GCN;
import eu.f3rog.automat.compiler.util.ProcessorError;
import eu.f3rog.automat.compiler.util.StringUtils;

/**
 * Class {@link ActivityInjectorBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class ActivityInjectorBuilder extends BaseClassBuilder {

    private static final String METHOD_NAME_INJECT = "inject";
    private static final String EXTRA_ID_FORMAT = "%s.%s-%s";

    private List<VariableElement> mExtras = new ArrayList<>();

    public ActivityInjectorBuilder(ClassName arg) throws ProcessorError {
        super(GCN.ACTIVITY_INJECTOR, arg);
    }

    @Override
    protected String getPackage() {
        return getArgClassName().packageName();
    }

    @Override
    public void start() throws ProcessorError {
        super.start();

        getBuilder().addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    @Override
    public void end() throws ProcessorError {
        super.end();

        addInjectMethod();
    }

    public void addExtra(VariableElement e) {
        mExtras.add(e);
    }

    public void addInjectMethod() {
        String target = "target";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_INJECT)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getArgClassName(), target);

        method.beginControlFlow("if ($N.getIntent() == null || $N.getIntent().getExtras() == null)", target, target)
                .addStatement("return")
                .endControlFlow();

        String extras = "extras";
        method.addStatement("$T $N = $N.getIntent().getExtras()", Bundle.class, extras, target);

        for (int i = 0; i < mExtras.size(); i++) {
            VariableElement extra = mExtras.get(i);
            method.addStatement("$N.$N = $N.$N($S)",
                    target, extra.getSimpleName(),
                    extras, getExtraGetterName(ClassName.get(extra.asType())), getExtraId(extra));
        }

        getBuilder().addMethod(method.build());
    }

    /**
     * Returns getter for given Extra type.
     */
    private String getExtraGetterName(TypeName typeName) {
        if (typeName.isPrimitive()) {
            return String.format("get%s", StringUtils.startUpperCase(typeName.toString()));
        } else if (typeName.equals(ClassName.get(String.class))) {
            return "getString";
        } else {
            return "get";
        }
    }

    public String getExtraId(VariableElement extra) {
        return String.format(EXTRA_ID_FORMAT,
                getArgClassName().packageName(), getArgClassName().simpleName(), extra.getSimpleName().toString());
    }

    public List<VariableElement> getExtras() {
        return mExtras;
    }
}
