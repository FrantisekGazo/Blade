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

    public enum MethodWeaveType {

        AT_BEGINNING("^"), BEFORE_SUPER("<"), AFTER_SUPER(">");

        private String mSign;

        MethodWeaveType(String sign) {
            mSign = sign;
        }

        public String getSign() {
            return mSign;
        }

        public static MethodWeaveType from(String into) {
            for (MethodWeaveType i : values()) {
                if (i.getSign().equals(into)) {
                    return i;
                }
            }
            return null;
        }
    }

    public enum WeavePriority {

        NORMAL(0), HIGHER(1);

        private final int mNum;

        WeavePriority(int num) {
            mNum = num;
        }

        public int getNum() {
            return mNum;
        }

        public static WeavePriority from(int number) {
            for (WeavePriority p : values()) {
                if (p.getNum() == number) {
                    return p;
                }
            }
            return null;
        }
    }

    public static final class Into {

        private final MethodWeaveType mMethodWeaveType;
        private final WeavePriority mPriority;
        private final String mMethodName;

        public Into(MethodWeaveType methodWeaveType, WeavePriority priority, String methodName) {
            mPriority = priority;
            mMethodWeaveType = methodWeaveType;
            mMethodName = methodName;
        }

        public MethodWeaveType getMethodWeaveType() {
            return mMethodWeaveType;
        }

        public WeavePriority getPriority() {
            return mPriority;
        }

        public String getMethodName() {
            return mMethodName;
        }
    }

    public static Into parseInto(final String into) {
        int number = Integer.valueOf(into.substring(0, 1));
        WeavePriority priority = WeavePriority.from(number);
        String type = into.substring(1, 2);
        MethodWeaveType weaveType = MethodWeaveType.from(type);
        String actualName = into.substring(2);
        return new Into(weaveType, priority, actualName);
    }

    public static IWeaveInto weave() {
        return new Implementation();
    }

    public interface IWeaveInto extends IWeaveBuild {

        IMethodWeaveStatement method(String methodName, Class... args);

        IWeaveStatement constructor(Class... args);

        IWeaveStatement field();

    }

    public interface IMethodWeaveStatement extends IWeaveStatement {

        IMethodWeaveStatement placed(MethodWeaveType type);

        IMethodWeaveStatement withPriority(WeavePriority priority);

    }

    public interface IWeaveStatement extends IWeaveBuild {

        IWeaveStatement withStatement(String statement, Object... args);

    }

    public interface IWeaveBuild {

        IWeaveInto and();

        AnnotationSpec build();

    }

    private static final class Implementation
            implements IWeaveInto, IMethodWeaveStatement {

        private AnnotationSpec.Builder mContainerAnnotationBuilder;
        private String mInto;
        private Object[] mIntoArgs;
        private StringBuilder mStatement = new StringBuilder();
        private MethodWeaveType mMethodWeaveType = null;
        private WeavePriority mWeavePriority = null;

        @Override
        public IMethodWeaveStatement method(String methodName, Class... args) {
            mInto = methodName;
            mIntoArgs = toString(args);
            mMethodWeaveType = MethodWeaveType.AT_BEGINNING;
            mWeavePriority = WeavePriority.NORMAL;
            return this;
        }

        @Override
        public IMethodWeaveStatement placed(MethodWeaveType type) {
            mMethodWeaveType = type;
            return this;
        }

        @Override
        public IMethodWeaveStatement withPriority(WeavePriority priority) {
            mWeavePriority = priority;
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
                String into = mInto;
                if (mMethodWeaveType != null) {
                    into = mMethodWeaveType.getSign() + into;
                }
                if (mWeavePriority != null) {
                    into = mWeavePriority.getNum() + into;
                }
                AnnotationSpec.Builder a = AnnotationSpec.builder(Weave.class)
                        .addMember("into", "$S", into);

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