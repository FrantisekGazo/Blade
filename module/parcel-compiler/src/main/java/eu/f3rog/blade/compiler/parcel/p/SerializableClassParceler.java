package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.MethodSpec;

import java.io.Serializable;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link SerializableClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class SerializableClassParceler implements ClassParceler {

    @Override
    public Class type() {
        return Serializable.class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.writeSerializable($N.$N)", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.$N = ($T) $N.readSerializable()", object, e.getSimpleName(), e.asType(), parcel);
    }

}
