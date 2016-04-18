package eu.f3rog.blade.compiler.parcel.p;

import android.util.SparseArray;

/**
 * Class {@link SparseArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class SparseArrayParceler implements ClassParceler {

    @Override
    public Class type() {
        return SparseArray.class;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeSparseArray((java.util.SparseArray) %s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("(%s) %s.readSparseArray(%s)", CallFormat.Arg.TYPE, CallFormat.Arg.PARCEL, CallFormat.Arg.CLASS_LOADER_OR_NULL);
    }
}
