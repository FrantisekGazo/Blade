package eu.f3rog.blade.compiler.parcel.p;

/**
 * Class {@link PrimitiveArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class PrimitiveArrayParceler implements ClassParceler {

    private final Class mClass;
    private final String mArrayType;

    public PrimitiveArrayParceler(Class clazz, String arrayType) {
        mClass = clazz;
        mArrayType = arrayType;
    }

    @Override
    public Class type() {
        return mClass;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.write" + mArrayType + "Array(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.create" + mArrayType + "Array()", CallFormat.Arg.PARCEL);
    }
}
