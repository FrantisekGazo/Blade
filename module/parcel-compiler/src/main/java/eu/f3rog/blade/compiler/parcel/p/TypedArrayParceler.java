package eu.f3rog.blade.compiler.parcel.p;

import android.os.Parcelable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

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
        String t = e.asType().toString();
        int i = t.lastIndexOf(".");
        TypeName tn = ClassName.get(t.substring(0, i), Parceler.removeArrayParenthesis(t.substring(i + 1)));
        method.addStatement("$N.$N = $N.createTypedArray($T.CREATOR)", object, e.getSimpleName(), parcel, tn);
    }

}
