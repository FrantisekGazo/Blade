package eu.f3rog.blade.compiler.parcel.p;

/**
 * Class {@link BooleanClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class BooleanClassParceler implements ClassParceler {

    @Override
    public Class type() {
        return boolean.class;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeByte((byte) (%s ? 1 : 0))", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("$N.readByte() > 0", CallFormat.Arg.PARCEL);
    }
}
