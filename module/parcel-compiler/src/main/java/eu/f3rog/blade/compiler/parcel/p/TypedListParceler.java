package eu.f3rog.blade.compiler.parcel.p;

import android.os.Parcelable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link TypedListParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
@Deprecated
final class TypedListParceler implements ClassParceler {

    @Override
    public Class type() {
        return Parcelable.class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.beginControlFlow("if ($N.$N == null)", object, e.getSimpleName())
                .addStatement("$N.writeString(null)", parcel)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$N.writeString($N.$N.getClass().getCanonicalName())", parcel, object, e.getSimpleName())
                .addStatement("$N.writeTypedList($N.$N)", parcel, object, e.getSimpleName())
                .endControlFlow();
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        ParameterizedTypeName tn = (ParameterizedTypeName) ClassName.get(e.asType());

        method.addStatement("$N = $N.readString()", Parceler.TEMP_STRING, parcel)
                .beginControlFlow("if ($N != null)", Parceler.TEMP_STRING)
                .beginControlFlow("try")
                .addStatement("$N.$N = ($T) Class.forName($N).newInstance()", object, e.getSimpleName(), e.asType(), Parceler.TEMP_STRING)
                .addStatement("$N.readTypedList($N.$N, $T.CREATOR)", parcel, object, e.getSimpleName(), tn.typeArguments.get(0))
                .endControlFlow()
                .beginControlFlow("catch ($T e)", Exception.class)
                .addStatement("throw new $T($S)", IllegalStateException.class, "List implementation is missing empty constructor.")
                .endControlFlow()
                .endControlFlow();
    }

}
