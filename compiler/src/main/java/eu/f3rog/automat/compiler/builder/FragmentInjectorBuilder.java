package eu.f3rog.automat.compiler.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import eu.f3rog.automat.compiler.name.GCN;
import eu.f3rog.automat.compiler.util.ProcessorError;
import eu.f3rog.automat.core.BundleWrapper;

/**
 * Class {@link FragmentInjectorBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class FragmentInjectorBuilder extends BaseInjectorBuilder {

    private List<VariableElement> mArgs = new ArrayList<>();

    public FragmentInjectorBuilder(ClassName arg) throws ProcessorError {
        super(GCN.CLASS_INJECTOR, arg);
    }

    public void addArg(VariableElement e) {
        mArgs.add(e);
    }

    public List<VariableElement> getArgs() {
        return mArgs;
    }

    @Override
    public void addInjectMethod() {
        String target = "target";
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_INJECT)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getArgClassName(), target);

        method.beginControlFlow("if ($N.getArguments() == null)", target)
                .addStatement("return")
                .endControlFlow();

        String args = "args";
        method.addStatement("$T $N = $T.from($N.getArguments())", BundleWrapper.class, args, BundleWrapper.class, target);

        for (int i = 0; i < mArgs.size(); i++) {
            VariableElement arg = mArgs.get(i);
            method.addStatement("$N.$N = $N.get($S, $N.$N)",
                    target, arg.getSimpleName(),
                    args, getExtraId(arg), target, arg.getSimpleName());
        }

        getBuilder().addMethod(method.build());
    }
}
