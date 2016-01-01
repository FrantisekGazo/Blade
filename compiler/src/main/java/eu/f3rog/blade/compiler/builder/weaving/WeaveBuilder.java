package eu.f3rog.blade.compiler.builder.weaving;

import com.squareup.javapoet.AnnotationSpec;

import eu.f3rog.blade.core.Weave;

/**
 * Class {@link eu.f3rog.blade.compiler.builder.weaving.WeaveBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-12-31
 */
public final class WeaveBuilder {

    public static IWeaveBuild into(String methodName, Class... args) {
        Implementation implementation = new Implementation();
        return implementation.into(methodName, args);
    }

    private static final class Implementation
            implements IWeaveInto {

        private String mInto;
        private String[] mIntoArgs;

        @Override
        public IWeaveBuild into(String methodName, Class... args) {
            mInto = methodName;
            mIntoArgs = toString(args);
            return this;
        }

        @Override
        public AnnotationSpec build() {
            return AnnotationSpec.builder(Weave.class)
                    .addMember("into", "$S", mInto)
                    .addMember("args", formatFor(mIntoArgs.length), mIntoArgs)
                    .build();
        }

        private String[] toString(final Class... classes) {
            String[] array = new String[classes.length];
            for (int i = 0; i < array.length; i++) {
                array[i] = classes[i].getName();
            }
            return array;
        }

        private String formatFor(final int count) {
            StringBuilder format = new StringBuilder();
            format.append("{");
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    format.append(", ");
                }
                format.append("$S");
            }
            format.append("}");
            return format.toString();
        }
    }

}
