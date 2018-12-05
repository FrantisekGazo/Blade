package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/**
 * Class {@link ObjectClassParceler}
 *
 * @author FrantisekGazo
 */
final class ObjectClassParceler implements BaseParceler {

    @Override
    public TypeName type() {
        return ClassName.OBJECT;
    }

    @Override
    public CallFormat writeCall() {
        return new CallFormat("%s.writeValue(%s)", CallFormat.Arg.PARCEL, CallFormat.Arg.TARGET_GETTER);
    }

    @Override
    public CallFormat readCall() {
        return new CallFormat("(%s) %s.readValue(%s)", CallFormat.Arg.TYPE, CallFormat.Arg.PARCEL, CallFormat.Arg.CLASS_LOADER_OR_NULL);
    }
}
