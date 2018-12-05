package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/**
 * Class {@link StringParceler}
 *
 * @author FrantisekGazo
 */
final class StringParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ClassName.get(String.class);
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeString(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("%s.readString()", CallFormat.Arg.PARCEL);
    }
}
