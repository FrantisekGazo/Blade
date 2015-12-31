package eu.f3rog.blade.compiler.builder;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.lang.model.element.Modifier;

import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link WeaveBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-12-29
 */
public class WeaveBuilder extends BaseClassBuilder {

    public WeaveBuilder() throws ProcessorError {
        super(BuilderType.ANNOTATION, GCN.WEAVE, GPN.BLADE);
    }

    @Override
    public void start() throws ProcessorError {
        super.start();
        // set as PUBLIC
        getBuilder().addModifiers(Modifier.PUBLIC);
        // add annotations
        getBuilder().addAnnotation(
                AnnotationSpec.builder(Target.class)
                        .addMember("value", "java.lang.annotation.ElementType.TYPE")
                        .build()
        );
        getBuilder().addAnnotation(
                AnnotationSpec.builder(Retention.class)
                        .addMember("value", "java.lang.annotation.RetentionPolicy.RUNTIME")
                        .build()
        );
        // add fields
        getBuilder().addMethod(
                MethodSpec.methodBuilder("value")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(String[].class)
                        .build()
        );
    }
}
