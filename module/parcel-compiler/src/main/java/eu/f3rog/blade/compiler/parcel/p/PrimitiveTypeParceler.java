package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

import static eu.f3rog.blade.compiler.util.StringUtils.startUpperCase;

/**
 * Class {@link PrimitiveTypeParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class PrimitiveTypeParceler implements ClassParceler {

    private final Class mClass;

    public PrimitiveTypeParceler(Class clazz) {
        mClass = clazz;
    }

    private String typeName() {
        return startUpperCase(type().getSimpleName());
    }

    @Override
    public Class type() {
        return mClass;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.write" + typeName() + "($N.$N)", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.$N = $N.read" + typeName() + "()", object, e.getSimpleName(), parcel);
    }

}
