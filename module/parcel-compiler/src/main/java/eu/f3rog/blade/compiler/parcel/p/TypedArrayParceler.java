package eu.f3rog.blade.compiler.parcel.p;

import android.os.Parcelable;

/**
 * Class {@link TypedArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class TypedArrayParceler implements ClassParceler {

    @Override
    public Class type() {
        return Parcelable[].class;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeTypedArray(%s, 0)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.createTypedArray(%s.CREATOR)", CallFormat.Arg.PARCEL, CallFormat.Arg.RAW_TYPE);
    }
}
