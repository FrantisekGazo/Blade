package eu.f3rog.blade.compiler.builder.annotation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import eu.f3rog.blade.core.GeneratedFor;

/**
 * Class {@link GeneratedForBuilder}
 *
 * @author FrantisekGazo
 * @version 2016-03-07
 */
public class GeneratedForBuilder {

    private GeneratedForBuilder() {}

    public static AnnotationSpec buildFor(Class cls) {
        return AnnotationSpec.builder(GeneratedFor.class)
                .addMember("value", "$T.class", cls)
                .build();
    }

    public static AnnotationSpec buildFor(ClassName className) {
        return AnnotationSpec.builder(GeneratedFor.class)
                .addMember("value", "$T.class", className)
                .build();
    }
}
