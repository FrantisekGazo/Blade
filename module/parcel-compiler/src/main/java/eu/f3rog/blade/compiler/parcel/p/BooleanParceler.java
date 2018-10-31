package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.TypeName;

/**
 * Class {@link BooleanParceler}
 *
 * @author FrantisekGazo
 */
final class BooleanParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return TypeName.BOOLEAN;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeByte((byte) (%s ? 1 : 0))", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.readByte() > 0", CallFormat.Arg.PARCEL);
    }
}
