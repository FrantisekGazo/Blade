package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link ClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
interface ClassParceler {

    Class type();

    void write(VariableElement e, MethodSpec.Builder method, String parcel, String object);

    void read(VariableElement e, MethodSpec.Builder method, String parcel, String object);

}
