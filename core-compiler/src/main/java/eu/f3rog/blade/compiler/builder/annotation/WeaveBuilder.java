package eu.f3rog.blade.compiler.builder.annotation;

import com.squareup.javapoet.AnnotationSpec;

import eu.f3rog.blade.core.Weave;
import eu.f3rog.blade.core.Weaves;

/**
 * Class {@link WeaveBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-12-31
 */
public final class WeaveBuilder {

    public static IWeaveInto weave() {
        return new Implementation();
    }

    public interface IWeaveInto extends IWeaveBuild {

        IWeaveStatement method(String methodName, Class... args);

        IWeaveStatement constructor(Class... args);

        IWeaveStatement field();

    }

    public interface IWeaveStatement extends IWeaveBuild {

        IWeaveStatement withStatement(String statement, Object... args);

    }

    public interface IWeaveBuild {

        IWeaveInto and();

        AnnotationSpec build();

    }

    private static final class Implementation
            implements IWeaveInto, IWeaveStatement {

        private AnnotationSpec.Builder mContainerAnnotationBuilder;
        private String mInto;
        private Object[] mIntoArgs;
        private StringBuilder mStatement = new StringBuilder();

        @Override
        public IWeaveStatement method(String methodName, Class... args) {
            mInto = methodName;
            mIntoArgs = toString(args);
            return this;
        }

        @Override
        public IWeaveStatement constructor(Class... args) {
            mInto = Weave.WEAVE_CONSTRUCTOR;
            mIntoArgs = toString(args);
            return this;
        }

        @Override
        public IWeaveStatement field() {
            mInto = Weave.WEAVE_FIELD;
            return this;
        }

        @Override
        public IWeaveStatement withStatement(String statement, Object... args) {
            mStatement.append(String.format(statement, args));
            return this;
        }

        @Override
        public IWeaveInto and() {
            if (mContainerAnnotationBuilder == null) {
                mContainerAnnotationBuilder = AnnotationSpec.builder(Weaves.class);
            }

            AnnotationSpec annotationSpec = build(false);
            mContainerAnnotationBuilder.addMember("value", "$L", annotationSpec);

            // clear
            mInto = null;
            mIntoArgs = null;
            mStatement = new StringBuilder();

            return this;
        }

        @Override
        public AnnotationSpec build() {
            return build(true);
        }

        private AnnotationSpec build(boolean isFinal) {
            if (isFinal) {
                if (mContainerAnnotationBuilder != null) {
                    and();
                    return mContainerAnnotationBuilder.build();
                } else {
                    return build(false);
                }
            } else {
                AnnotationSpec.Builder a = AnnotationSpec.builder(Weave.class)
                        .addMember("into", "$S", mInto);

                if (mIntoArgs != null && mIntoArgs.length > 0) {
                    a.addMember("args", formatFor("$S", mIntoArgs.length), mIntoArgs);
                }
                a.addMember("statement", "$S", mStatement.toString());
                return a.build();
            }
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