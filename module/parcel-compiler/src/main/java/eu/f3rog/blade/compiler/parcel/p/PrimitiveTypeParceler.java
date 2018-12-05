package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.TypeName;

import static eu.f3rog.blade.compiler.util.StringUtils.startUpperCase;

/**
 * Class {@link PrimitiveTypeParceler}
 *
 * @author FrantisekGazo
 */
final class PrimitiveTypeParceler implements BaseParceler {

    private final TypeName mType;

    public PrimitiveTypeParceler(TypeName type) {
        if (!type.isPrimitive()) {
            throw new IllegalArgumentException("Unsupported " + type);
        }
        mType = type;
    }

    private String typeName() {
        return startUpperCase(mType.toString());
    }

    @Override
    public TypeName type() {
        return mType;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.write" + typeName() + "(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.read" + typeName() + "()", CallFormat.Arg.PARCEL);
    }
}
