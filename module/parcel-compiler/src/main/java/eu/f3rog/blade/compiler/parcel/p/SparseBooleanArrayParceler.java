package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.TypeName;

import eu.f3rog.blade.compiler.name.ClassNames;

/**
 * Class {@link SparseBooleanArrayParceler}
 *
 * @author FrantisekGazo
 */
final class SparseBooleanArrayParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ClassNames.SparseBooleanArray.get();
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
