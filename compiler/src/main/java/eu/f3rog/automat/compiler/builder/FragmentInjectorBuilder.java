package eu.f3rog.automat.compiler.builder;

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
        method.addStatement("$T $N = $N.getArguments()", Bundle.class, args, target);

        for (int i = 0; i < mArgs.size(); i++) {
            VariableElement arg = mArgs.get(i);
            TypeName type = ClassName.get(arg.asType());
            method.addStatement("$N.$N = ($T) $N.$N($S)",
                    target, arg.getSimpleName(), type,
                    args, getExtraGetterName(type), getExtraId(arg));
        }

        getBuilder().addMethod(method.build());
    }
}
