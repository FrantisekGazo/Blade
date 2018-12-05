package eu.f3rog.blade.compiler.extra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Extra;
import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.builder.annotation.GeneratedForBuilder;
import eu.f3rog.blade.compiler.module.BundleUtils;
import eu.f3rog.blade.compiler.name.ClassNames;
import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.name.NameUtils;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.isSubClassOf;

/**
 * Class {@link IntentBuilderBuilder}
 *
 * @author FrantisekGazo
 */
public final class IntentBuilderBuilder
        extends BaseClassBuilder {

    private static final String METHOD_NAME_FOR = "for%s";
    private static final String METHOD_NAME_START = "start%s";

    public IntentBuilderBuilder() throws ProcessorError {
        super(GCN.INTENT_MANAGER, GPN.BLADE);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        getBuilder().addModifiers(Modifier.PUBLIC);
    }

    public void addMethodsFor(final TypeElement typeElement) throws ProcessorError {
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            return;
        }

        List<VariableElement> extras = new ArrayList<>();

        List<? extends Element> elements = ProcessorUtils.getElementUtils().getAllMembers(typeElement);
        for (Element e : elements) {
            if (e instanceof VariableElement && e.getAnnotation(Extra.class) != null) {
                extras.add((VariableElement) e);
            }
        }

        integrate(ClassName.get(typeElement), extras, isSubClassOf(typeElement, ClassNames.Service.get()));
    }

    private void integrate(final ClassName activityClassName,
                           final List<VariableElement> fields,
                           final boolean isService) throws ProcessorError {
        final String forName = getMethodName(METHOD_NAME_FOR, activityClassName);
        final String context = "context";
        final String intent = "intent";
        final String extras = "extras";
        // build FOR method
        final MethodSpec.Builder forMethod = MethodSpec.methodBuilder(forName)
                .addAnnotation(GeneratedForBuilder.buildFor(activityClassName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassNames.Context.get(), context)
                .returns(ClassNames.Intent.get());
        // build START method
        final MethodSpec.Builder startMethod = MethodSpec.methodBuilder(getMethodName(METHOD_NAME_START, activityClassName))
                .addAnnotation(GeneratedForBuilder.buildFor(activityClassName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassNames.Context.get(), context);

        forMethod.addStatement("$T $N = new $T($N, $T.class)",
               ClassNames.Intent.get(), intent, ClassNames.Intent.get(), context, activityClassName);
        forMethod.addStatement("$T $N = new $T()",
                        BundleWrapper.class, extras, BundleWrapper.class);
        startMethod.addCode("$N.$N($N($N",
                context, (isService) ? "startService" : "startActivity", forName, context);


        final ProcessorUtils.IGetter<Extra, Class<?>> classGetter = new ProcessorUtils.IGetter<Extra, Class<?>>() {
            @Override
            public Class<?> get(final Extra a) {
                return a.value();
            }
        };

        for (int i = 0, c = fields.size(); i < c; i++) {
            final VariableElement field = fields.get(i);
            final Type type = ProcessorUtils.getBoundedType(field);
            final TypeName typeName = ClassName.get(type);
            final String name = field.getSimpleName().toString();

            forMethod.addParameter(typeName, name);
            final BundleUtils.BundledField bundledField = BundleUtils.getBundledField(field, Extra.class, classGetter);
            BundleUtils.putToBundle(forMethod, null, bundledField, ExtraHelperModule.EXTRA_ID_FORMAT, extras);

            startMethod.addParameter(typeName, name);
            startMethod.addCode(", $N", name);
        }

        forMethod.addStatement("$N.putExtras($N.getBundle())", intent, extras)
                .addStatement("return $N", intent);
        startMethod.addCode("));\n");

        // add methods
        getBuilder().addMethod(forMethod.build());
        getBuilder().addMethod(startMethod.build());
    }

    private String getMethodName(final String format, final ClassName activityName) {
        return String.format(format, NameUtils.getNestedName(activityName, ""));
    }
}
