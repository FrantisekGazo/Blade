package eu.f3rog.blade.compiler.parcel.p;

/**
 * Class {@link ObjectClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class ObjectClassParceler implements ClassParceler {

    @Override
    public Class type() {
        return Object.class;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeValue(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("(%s) %s.readValue(%s)", CallFormat.Arg.TYPE, CallFormat.Arg.PARCEL, CallFormat.Arg.CLASS_LOADER_OR_NULL);
    }
}
