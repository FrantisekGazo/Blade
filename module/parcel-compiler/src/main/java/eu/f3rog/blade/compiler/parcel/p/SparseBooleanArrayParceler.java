package eu.f3rog.blade.compiler.parcel.p;

import android.util.SparseBooleanArray;

/**
 * Class {@link SparseBooleanArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class SparseBooleanArrayParceler implements ClassParceler {

    @Override
    public Class type() {
        return SparseBooleanArray.class;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeSparseBooleanArray(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.readSparseBooleanArray()", CallFormat.Arg.PARCEL);
    }
}
