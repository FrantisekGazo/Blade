package eu.f3rog.blade.compiler.builder.annotation;

/**
 * Class {@link WeaveParser}
 *
 * @author FrantisekGazo
 */
public final class WeaveParser {

    public static final class Into {

        private final WeaveBuilder.MethodWeaveType mMethodWeaveType;
        private final WeaveBuilder.WeavePriority mPriority;
        private final String mMethodName;

        private Into(WeaveBuilder.MethodWeaveType methodWeaveType, WeaveBuilder.WeavePriority priority, String methodName) {
            mPriority = priority;
            mMethodWeaveType = methodWeaveType;
            mMethodName = methodName;
        }

        public WeaveBuilder.MethodWeaveType getMethodWeaveType() {
            return mMethodWeaveType;
        }

        public WeaveBuilder.WeavePriority getPriority() {
            return mPriority;
        }

        public String getMethodName() {
            return mMethodName;
        }
    }

    public static Into parseInto(final String into) {
        int number = Integer.valueOf(into.substring(0, 1));
        WeaveBuilder.WeavePriority priority = WeaveBuilder.WeavePriority.from(number);
        String type = into.substring(1, 2);
        WeaveBuilder.MethodWeaveType weaveType = WeaveBuilder.MethodWeaveType.from(type);
        String actualName = into.substring(2);
        return new Into(weaveType, priority, actualName);
    }
}