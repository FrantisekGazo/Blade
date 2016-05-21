package eu.f3rog.blade.compiler.parcel.p;

import java.io.Serializable;

/**
 * Class {@link SerializableClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class SerializableClassParceler implements ClassParceler {

    @Override
    public Class type() {
        return Serializable.class;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeSerializable(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("(%s) %s.readSerializable()", CallFormat.Arg.TYPE, CallFormat.Arg.PARCEL);
    }
}
