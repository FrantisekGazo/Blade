package eu.f3rog.blade.compiler.parcel.p;

import android.os.Parcelable;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.getRawType;

/**
 * Class {@link TypedArrayParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
final class TypedArrayParceler implements ClassParceler {

    @Override
    public Class type() {
        return Parcelable[].class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.addStatement("$N.writeTypedArray($N.$N, 0)", parcel, object, e.getSimpleName());
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        ArrayTypeName tn = (ArrayTypeName) ClassName.get(e.asType());
        TypeName rawType = getRawType(tn.componentType);
        if (rawType == null) {
            throw new IllegalStateException();
        }
        method.addStatement("$N.$N = $N.createTypedArray($T.CREATOR)", object, e.getSimpleName(), parcel, rawType);
    }

}
