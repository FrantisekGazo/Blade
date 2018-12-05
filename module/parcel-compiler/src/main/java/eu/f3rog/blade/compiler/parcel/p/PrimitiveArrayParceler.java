package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.TypeName;

/**
 * Class {@link PrimitiveArrayParceler}
 *
 * @author FrantisekGazo
 */
final class PrimitiveArrayParceler implements BaseParceler {

    private final TypeName mTypeName;
    private final String mArrayType;

    public PrimitiveArrayParceler(TypeName typeName, String arrayType) {
        mTypeName = ArrayTypeName.of(typeName);
        mArrayType = arrayType;
    }

    @Override
    public TypeName type() {
        return mTypeName;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.write" + mArrayType + "Array(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.create" + mArrayType + "Array()", CallFormat.Arg.PARCEL);
    }
}
