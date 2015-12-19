package eu.f3rog.blade.compiler.builder;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link MiddleManBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class MiddleManBuilder
        extends BaseClassBuilder {

    public MiddleManBuilder() throws ProcessorError {
        super(GCN.MIDDLE_MAN, GPN.BLADE);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();

        getBuilder().addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    public void addCall(BaseClassBuilder builder, String methodName) {
        String target = "target";
        MethodSpec.Builder method = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target)
                .addStatement("$T.$N($N)", builder.getClassName(), methodName, target);

        getBuilder().addMethod(method.build());
    }
}
