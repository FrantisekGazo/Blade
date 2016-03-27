package eu.f3rog.blade.compiler.parcel.p;

import android.util.SparseBooleanArray;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link SparseBooleanArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class SparseBooleanArrayParceler implements ClassParceler {

    @Override
    public Class type() {
        return SparseBooleanArray.class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.writeSparseBooleanArray($N.$N)", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.$N = $N.readSparseBooleanArray()", object, e.getSimpleName(), parcel);
    }

}
