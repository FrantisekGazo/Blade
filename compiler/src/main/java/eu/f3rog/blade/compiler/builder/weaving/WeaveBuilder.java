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

    public static AnnotationSpec forField(String statement, Object... args) {
        AnnotationSpec.Builder a = AnnotationSpec.builder(Weave.class);
        if (statement != null) {
            a.addMember("statement", "$S", String.format(statement, args));
        }
        return a.build();
    }

    public static IWeaveStatement into(String methodName, Class... args) {
        Implementation implementation = new Implementation();
        return implementation.into(methodName, args);
    }

    public static IWeaveStatement intoConstructor(Class... args) {
        Implementation implementation = new Implementation();
        return implementation.into("", args);
    }

    public interface IWeaveInto extends IWeaveBuild {
        IWeaveStatement into(String methodName, Class... args);
    }

    public interface IWeaveStatement extends IWeaveBuild {
        IWeaveStatement addStatement(String statement, Object... args);
    }

    public interface IWeaveBuild {
        AnnotationSpec build();
    }

    private static final class Implementation
            implements IWeaveInto, IWeaveStatement {

        private String mInto;
        private Object[] mIntoArgs;
        private StringBuilder mStatement = new StringBuilder();

        @Override
        public IWeaveStatement into(String methodName, Class... args) {
            mInto = methodName;
            mIntoArgs = toString(args);
            return this;
        }

        @Override
        public IWeaveStatement addStatement(String statement, Object... args) {
            mStatement.append(String.format(statement, args));
            return this;
        }

        @Override
        public AnnotationSpec build() {
            AnnotationSpec.Builder a = AnnotationSpec.builder(Weave.class)
                    .addMember("into", "$S", mInto);

            if (mIntoArgs != null && mIntoArgs.length > 0) {
                a.addMember("args", formatFor("$S", mIntoArgs.length), mIntoArgs);
            }
            a.addMember("statement", "$S", mStatement.toString());

            return a.build();
        }

        private String[] toString(final Class... classes) {
            String[] array = new String[classes.length];
            for (int i = 0; i < array.length; i++) {
                array[i] = classes[i].getName();
            }
            return array;
        }
    }

    private static String formatFor(String f, final int count) {
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