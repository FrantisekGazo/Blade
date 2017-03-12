package eu.f3rog.blade.compiler.builder.annotation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.core.WeaveInto;

public class WeaveIntoBuilder {
    public static AnnotationSpec buildFor(ClassName className) {
        return AnnotationSpec.builder(WeaveInto.class)
                .addMember("target", "\"" + ProcessorUtils.fullName(className) + "\"")
                .build();
    }
}
