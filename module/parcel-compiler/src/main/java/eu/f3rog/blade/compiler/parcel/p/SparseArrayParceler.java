package eu.f3rog.blade.compiler.parcel.p;

import android.util.SparseArray;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link SparseArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class SparseArrayParceler implements ClassParceler {

    @Override
    public Class type() {
        return SparseArray.class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.writeSparseArray(($T) $N.$N)", parcel, SparseArray.class, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.$N = ($T) $N.readSparseArray($T.class.getClassLoader())", object, e.getSimpleName(), e.asType(), parcel, SparseArray.class);
    }

}
