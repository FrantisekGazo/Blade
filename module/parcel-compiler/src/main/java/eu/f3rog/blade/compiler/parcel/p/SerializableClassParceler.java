package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.Serializable;

/**
 * Class {@link SerializableClassParceler}
 *
 * @author FrantisekGazo
 */
final class SerializableClassParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ClassName.get(Serializable.class);
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeSerializable(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("(%s) %s.readSerializable()", CallFormat.Arg.TYPE, CallFormat.Arg.PARCEL);
    }
}
