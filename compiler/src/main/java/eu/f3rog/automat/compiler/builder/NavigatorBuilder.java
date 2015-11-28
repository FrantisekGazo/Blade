package eu.f3rog.automat.compiler.builder;

import android.content.Context;
import android.content.Intent;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import eu.f3rog.automat.compiler.name.GCN;
import eu.f3rog.automat.compiler.name.GPN;
import eu.f3rog.automat.compiler.util.ProcessorError;

/**
 * Class {@link NavigatorBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-10-21
 */
public class NavigatorBuilder extends BaseClassBuilder {

    private static final String METHOD_NAME_FOR = "for%s";
    private static final String METHOD_NAME_START = "start%s";

    public NavigatorBuilder() throws ProcessorError {
        super(GCN.NAVIGATOR, GPN.AUTOMAT);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        getBuilder().addModifiers(Modifier.FINAL, Modifier.PUBLIC);
    }

    public void integrate(ActivityInjectorBuilder aib) throws ProcessorError {
        ClassName activityClassName = aib.getArgClassName();
        String forName = getMethodName(METHOD_NAME_FOR, activityClassName);
        String context = "context";
        String intent = "intent";
        // build FOR method
        MethodSpec.Builder forMethod = MethodSpec.methodBuilder(forName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Context.class, context)
                .returns(Intent.class);
        // build START method
        MethodSpec.Builder startMethod = MethodSpec.methodBuilder(getMethodName(METHOD_NAME_START, activityClassName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Context.class, context);

        forMethod.addStatement("$T $N = new $T($N, $T.class)", Intent.class, intent, Intent.class, context, activityClassName);
        startMethod.addCode("$N.startActivity($N($N", context, forName, context);
        for (VariableElement extra : aib.getExtras()) {
            TypeName typeName = ClassName.get(extra.asType());
            String name = extra.getSimpleName().toString();
            forMethod.addParameter(typeName, name);
            forMethod.addStatement("$N.putExtra($S, $N)", intent, aib.getExtraId(extra), name);

            startMethod.addParameter(typeName, name);
            startMethod.addCode(", $N", name);
        }
        forMethod.addStatement("return $N", intent);
        startMethod.addCode("));\n");
        // add methods
        getBuilder().addMethod(forMethod.build());
        getBuilder().addMethod(startMethod.build());
    }

    private String getMethodName(String format, ClassName activityName) {
        return String.format(format, activityName.simpleName());
    }

}
