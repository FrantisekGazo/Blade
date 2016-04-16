package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.util.ProcessorUtils;

/**
 * Class {@link ObjectClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class ObjectClassParceler implements ClassParceler {

    @Override
    public Class type() {
        return Object.class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.writeValue($N.$N)", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        TypeName rawType = ProcessorUtils.getRawType(e.asType());
        if (rawType != null) {
            method.addStatement("$N.$N = ($T) $N.readValue($T.class.getClassLoader())", object, e.getSimpleName(), e.asType(), parcel, rawType);
        } else {
            method.addStatement("$N.$N = ($T) $N.readValue(null)", object, e.getSimpleName(), e.asType(), parcel);
        }
    }

}
