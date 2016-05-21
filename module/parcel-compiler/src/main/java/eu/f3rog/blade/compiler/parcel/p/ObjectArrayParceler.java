package eu.f3rog.blade.compiler.parcel.p;

/**
 * Class {@link ObjectArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class ObjectArrayParceler implements ClassParceler {

    @Override
    public Class type() {
        return Object[].class;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeArray(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("(%s) %s.readArray(%s)", CallFormat.Arg.TYPE, CallFormat.Arg.PARCEL, CallFormat.Arg.CLASS_LOADER_OR_NULL);
    }

}
