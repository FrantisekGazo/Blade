package eu.f3rog.blade.compiler.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.getSuperClass;

/**
 * Class {@link ParametrizedTypeManager}
 *
 * @author FrantisekGazo
 * @version 2016-02-11
 */
public class ParametrizedTypeManager {

    private final TypeElement mTypeElement;

    public ParametrizedTypeManager(TypeElement typeElement) {
        mTypeElement = typeElement;
    }

    private void prepare() {
        TypeElement typeElement = mTypeElement;

        while (typeElement != null) {
            List<? extends TypeParameterElement> typeParams = typeElement.getTypeParameters();


            for (int i = 0; i < typeElement.getInterfaces().size(); i++) {
                TypeMirror itype = typeElement.getInterfaces().get(i);
                TypeName tn = ClassName.get(itype);
                if (tn instanceof ParameterizedTypeName) {
                    ParameterizedTypeName ptn = (ParameterizedTypeName) tn;
                }
            }

            typeElement = getSuperClass(typeElement);
        }
    }

    public TypeName getInterfaceParam(Class interfaceClass, int paramIndex) {
        return ClassName.get(String.class);
    }

    public TypeName getSuperclassParam(Class superClass, int paramIndex) {
        return ClassName.get(String.class);
    }

}
