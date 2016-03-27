package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link PrimitiveArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class PrimitiveArrayParceler implements ClassParceler {

    private final Class mClass;
    private final String mArrayType;

    public PrimitiveArrayParceler(Class clazz, String arrayType) {
        mClass = clazz;
        mArrayType = arrayType;
    }

    @Override
    public Class type() {
        return mClass;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.write" + mArrayType + "Array($N.$N)", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.$N = $N.create" + mArrayType + "Array()", object, e.getSimpleName(), parcel);
    }

}
