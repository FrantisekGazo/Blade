package eu.f3rog.blade.compiler.parcel.p;

import android.os.Parcelable;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link ParcelableClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class ParcelableClassParceler implements ClassParceler {

    @Override
    public Class type() {
        return Parcelable.class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.writeParcelable($N.$N, 0)", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.$N = ($T) $N.readParcelable($T.class.getClassLoader())", object, e.getSimpleName(), e.asType(), parcel, e.asType());
    }

}
