package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.TypeName;

import eu.f3rog.blade.compiler.name.ClassNames;

/**
 * Class {@link ParcelableClassParceler}
 *
 * @author FrantisekGazo
 */
final class ParcelableClassParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ClassNames.Parcelable.get();
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
