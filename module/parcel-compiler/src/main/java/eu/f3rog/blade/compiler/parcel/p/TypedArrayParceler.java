package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.TypeName;

import eu.f3rog.blade.compiler.name.ClassNames;

/**
 * Class {@link TypedArrayParceler}
 *
 * @author FrantisekGazo
 */
final class TypedArrayParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ArrayTypeName.of(ClassNames.Parcelable.get());
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
