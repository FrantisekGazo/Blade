package eu.f3rog.blade.compiler.module.extra;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Extra;
import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.isSubClassOf;

/**
 * Class {@link IntentBuilderBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-10-21
 */
public class IntentBuilderBuilder extends BaseClassBuilder {

    private static final String METHOD_NAME_FOR = "for%s";
    private static final String METHOD_NAME_START = "start%s";
    private static final String METHOD_NAME_START_FOR_RESULT = "start%sForResult"; // TODO

    public IntentBuilderBuilder() throws ProcessorError {
        super(GCN.INTENT_MANAGER, GPN.BLADE);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        getBuilder().addModifiers(Modifier.FINAL, Modifier.PUBLIC);
    }

    public void addMethodsFor(ProcessingEnvironment processingEnvironment, TypeElement typeElement) throws ProcessorError {
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            return;
        }

        List<VariableElement> extras = new ArrayList<>();

        List<? extends Element> elements = processingEnvironment.getElementUtils().getAllMembers(typeElement);
        for (Element e : elements) {
            if (e instanceof VariableElement && e.getAnnotation(Extra.class) != null) {
                extras.add((VariableElement) e);
            }
        }

        ClassName className = ClassName.get(typeElement);
        boolean isService = isSubClassOf(typeElement, Service.class);

        addMethodFor(className, extras);
        addMethodStart(className, extras, isService);
        if (!isService) {
            addMethodStartForResult(className, extras, Activity.class);
            addMethodStartForResult(className, extras, Fragment.class);
        }
    }

    private void addMethodFor(ClassName className, List<VariableElement> allExtras) throws ProcessorError {
        String forName = getMethodName(METHOD_NAME_FOR, className);
        String context = "context";
        String intent = "intent";
        String extras = "extras";
        // build FOR method
        MethodSpec.Builder forMethod = MethodSpec.methodBuilder(forName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Context.class, context)
                .returns(Intent.class);

        forMethod.addStatement("$T $N = new $T($N, $T.class)", Intent.class, intent, Intent.class, context, className)
                .addStatement("$T $N = new $T()", BundleWrapper.class, extras, BundleWrapper.class);

        for (VariableElement extra : allExtras) {
            TypeName typeName = ClassName.get(extra.asType());
            String name = extra.getSimpleName().toString();

            forMethod.addParameter(typeName, name);
            forMethod.addStatement("$N.put($S, $N)", extras, ExtraHelperModule.getExtraId(name), name);
        }
        forMethod.addStatement("$N.putExtras($N.getBundle())", intent, extras)
                .addStatement("return $N", intent);

        // add methods
        getBuilder().addMethod(forMethod.build());
    }

    private void addMethodStart(ClassName className, List<VariableElement> allExtras, boolean isService) throws ProcessorError {
        String forName = getMethodName(METHOD_NAME_FOR, className);
        String context = "context";
        // build START method
        MethodSpec.Builder startMethod = MethodSpec.methodBuilder(getMethodName(METHOD_NAME_START, className))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Context.class, context);

        startMethod.addCode("$N.$N($N($N", context,
                (isService) ? "startService" : "startActivity",
                forName, context);

        for (VariableElement extra : allExtras) {
            TypeName typeName = ClassName.get(extra.asType());
            String name = extra.getSimpleName().toString();

            startMethod.addParameter(typeName, name);
            startMethod.addCode(", $N", name);
        }
        startMethod.addCode("));\n");

        // add methods
        getBuilder().addMethod(startMethod.build());
    }

    private void addMethodStartForResult(ClassName className, List<VariableElement> allExtras, Class fromClass) throws ProcessorError {
        String forName = getMethodName(METHOD_NAME_FOR, className);
        String from = (fromClass == Activity.class) ? "activity" : "fragment";
        String requestCode = "requestCode";
        // build START FOR RESULT method
        MethodSpec.Builder startForResultMethod = MethodSpec.methodBuilder(getMethodName(METHOD_NAME_START_FOR_RESULT, className))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(fromClass, from)
                .addParameter(int.class, requestCode);

        startForResultMethod.addCode("$N.startActivityForResult($N($N", from, forName, from);
        if (fromClass == Fragment.class) {
            startForResultMethod.addCode(".getActivity()");
        }
        for (VariableElement extra : allExtras) {
            TypeName typeName = ClassName.get(extra.asType());
            String name = extra.getSimpleName().toString();

            startForResultMethod.addParameter(typeName, name);
            startForResultMethod.addCode(", $N", name);
        }
        startForResultMethod.addCode("), $N);\n", requestCode);

        // add methods
        getBuilder().addMethod(startForResultMethod.build());
    }

    private String getMethodName(String format, ClassName activityName) {
        return String.format(format, activityName.simpleName());
    }

}
