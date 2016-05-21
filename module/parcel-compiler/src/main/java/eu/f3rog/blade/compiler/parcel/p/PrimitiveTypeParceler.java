package eu.f3rog.blade.compiler.parcel.p;

import static eu.f3rog.blade.compiler.util.StringUtils.startUpperCase;

/**
 * Class {@link PrimitiveTypeParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class PrimitiveTypeParceler implements ClassParceler {

    private final Class mClass;

    public PrimitiveTypeParceler(Class clazz) {
        mClass = clazz;
    }

    private String typeName() {
        return startUpperCase(type().getSimpleName());
    }

    @Override
    public Class type() {
        return mClass;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.write" + typeName() + "(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.read" + typeName() + "()", CallFormat.Arg.PARCEL);
    }
}
