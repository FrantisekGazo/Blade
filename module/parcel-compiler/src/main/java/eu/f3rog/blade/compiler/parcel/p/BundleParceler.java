package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.TypeName;

import eu.f3rog.blade.compiler.name.ClassNames;

/**
 * Class {@link BundleParceler}
 *
 * @author FrantisekGazo
 */
final class BundleParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ClassNames.Bundle.get();
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeBundle(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.readBundle()", CallFormat.Arg.PARCEL);
    }
}
