package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.TypeName;

import eu.f3rog.blade.compiler.name.ClassNames;

/**
 * Class {@link SparseArrayParceler}
 *
 * @author FrantisekGazo
 */
final class SparseArrayParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ClassNames.SparseArray.get();
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
