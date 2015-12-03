package eu.f3rog.blade.compiler.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.core.BundleWrapper;

/**
 * Class {@link ActivityInjectorBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class ActivityInjectorBuilder extends BaseInjectorBuilder {

    private List<VariableElement> mExtras = new ArrayList<>();

    public ActivityInjectorBuilder(ClassName arg) throws ProcessorError {
        super(GCN.CLASS_INJECTOR, arg);
    }

    public void addExtra(VariableElement e) {
        mExtras.add(e);
    }

    public List<VariableElement> getExtras() {
        return mExtras;
    }

    @Override
    public void addInjectMethod() {
        String target = "target";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_INJECT)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(getArgClassName(), target);

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

        getBuilder().addMethod(method.build());
    }
}
