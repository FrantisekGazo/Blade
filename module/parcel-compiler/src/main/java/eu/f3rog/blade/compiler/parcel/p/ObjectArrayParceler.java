package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link ObjectArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class ObjectArrayParceler implements ClassParceler {

    @Override
    public Class type() {
        return Object[].class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.writeArray($N.$N)", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.$N = ($T) $N.readArray($T.class.getClassLoader())", object, e.getSimpleName(), e.asType(), parcel, e.asType());
    }

}
