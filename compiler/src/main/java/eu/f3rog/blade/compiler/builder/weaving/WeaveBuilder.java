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

    public static IWeaveUse into(String methodName, Class... args) {
        Implementation implementation = new Implementation();
        return implementation.into(methodName, args);
    }

    private static final class Implementation
            implements IWeaveInto, IWeaveUse {

        private String mInto;
        private String[] mIntoArgs;
        private Integer[] mUse;

        @Override
        public IWeaveUse into(String methodName, Class... args) {
            mInto = methodName;
            mIntoArgs = toString(args);
            return this;
        }

        @Override
        public IWeaveBuild use(Integer... argNumbers) {
            mUse = argNumbers;
            return this;
        }

        @Override
        public AnnotationSpec build() {
            if (mUse == null) {
                mUse = new Integer[0];
            }

            AnnotationSpec.Builder a = AnnotationSpec.builder(Weave.class)
                    .addMember("into", "$S", mInto);

            if (mIntoArgs != null && mIntoArgs.length > 0) {
                a.addMember("args", formatFor("$S", mIntoArgs.length), mIntoArgs);
            }
            if (mUse != null && mUse.length > 0) {
                a.addMember("use", formatFor("$L", mUse.length), mUse);
            }

            return a.build();
        }

        private String[] toString(final Class... classes) {
            String[] array = new String[classes.length];
            for (int i = 0; i < array.length; i++) {
                array[i] = classes[i].getName();
            }
            return array;
        }

        private String formatFor(String f, final int count) {
            StringBuilder format = new StringBuilder();
            format.append("{");
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    format.append(", ");
                }
                format.append(f);
            }
            format.append("}");
            return format.toString();
        }
    }

}
