package eu.f3rog.blade.compiler.module;

import com.squareup.javapoet.AnnotationSpec;

import blade.core.WeaveInto;

/**
 * Class {@link WeaveUtils}
 *
 * @author FrantisekGazo
 * @version 2015-12-31
 */
public class WeaveUtils {

    public static AnnotationSpec createWeaveAnnotation(String into) {
        return AnnotationSpec.builder(WeaveInto.class)
                .addMember("value", "$S", into)
                .build();
    }

}
