package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link MapParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
@Deprecated
final class MapParceler implements ClassParceler {

    @Override
    public Class type() {
        return Object.class;
    }

    @Override
    public void write(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        method.beginControlFlow("if ($N.$N == null)", object, e.getSimpleName())
                .addStatement("$N.writeString(null)", parcel)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$N.writeString($N.$N.getClass().getCanonicalName())", parcel, object, e.getSimpleName())
                .addStatement("$N.writeInt($N.$N.size())", parcel, object, e.getSimpleName())
                .beginControlFlow("for(Map.Entry entry : $N.$N.entrySet())", object, e.getSimpleName())
                .addStatement("$N.writeValue(entry.getKey())", parcel)
                .addStatement("$N.writeValue(entry.getValue())", parcel)
                .endControlFlow()
                .endControlFlow();
    }

    @Override
    public void read(VariableElement e, MethodSpec.Builder method, String parcel, String object) {
        ParameterizedTypeName tn = (ParameterizedTypeName) ClassName.get(e.asType());

        String count = "count";
        String key = "key";
        String value = "value";
        method.addStatement("$N = $N.readString()", Parceler.TEMP_STRING, parcel)
                .beginControlFlow("if ($N != null)", Parceler.TEMP_STRING)
                .beginControlFlow("try")
                .addStatement("$N.$N = ($T) Class.forName($N).newInstance()", object, e.getSimpleName(), e.asType(), Parceler.TEMP_STRING)
                .addStatement("$T $N = $N.readInt()", int.class, count, parcel)
                .beginControlFlow("for (int i = 0; i < $N; i++)", count)
                .addStatement("$T $N = ($T) $N.readValue(null)", tn.typeArguments.get(0), key, tn.typeArguments.get(0), parcel)
                .addStatement("$T $N = ($T) $N.readValue(null)", tn.typeArguments.get(1), value, tn.typeArguments.get(1), parcel)
                .addStatement("$N.$N.put($N, $N)", object, e.getSimpleName(), key, value)
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("catch ($T e)", Exception.class)
                .addStatement("throw new $T($S)", IllegalStateException.class, "Map implementation is missing empty constructor.")
                .endControlFlow()
                .endControlFlow();
    }

}
