package eu.f3rog.blade.compiler.parcel.p;

import android.os.Parcelable;

/**
 * Class {@link ParcelableClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class ParcelableClassParceler implements ClassParceler {

    @Override
    public Class type() {
        return Parcelable.class;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeParcelable(%s, 0)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("(%s) %s.readParcelable(%s)", CallFormat.Arg.TYPE, CallFormat.Arg.PARCEL, CallFormat.Arg.CLASS_LOADER_OR_NULL);
    }
}
