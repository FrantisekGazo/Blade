package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/**
 * Class {@link ObjectArrayParceler}
 *
 * @author FrantisekGazo
 */
final class ObjectArrayParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ArrayTypeName.of(ClassName.get(Object.class));
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeArray(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("(%s) %s.readArray(%s)", CallFormat.Arg.TYPE, CallFormat.Arg.PARCEL, CallFormat.Arg.CLASS_LOADER_OR_NULL);
    }
}
