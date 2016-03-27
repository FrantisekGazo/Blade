package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link BooleanClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class BooleanClassParceler implements ClassParceler {

    @Override
    public Class type() {
        return boolean.class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.writeByte((byte) ($N.$N ? 1 : 0))", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.$N = $N.readByte() > 0", object, e.getSimpleName(), parcel);
    }

}
