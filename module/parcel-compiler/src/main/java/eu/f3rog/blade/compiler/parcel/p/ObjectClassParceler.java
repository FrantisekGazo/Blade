package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.VariableElement;

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
        if (e instanceof Symbol.VarSymbol) {
            Symbol.VarSymbol variable = (Symbol.VarSymbol) e;

            StringBuilder sb = new StringBuilder();
            List<Symbol.TypeSymbol> typeParameters = variable.getTypeParameters();
            for (int i = 0, c = typeParameters.size(); i < c; i++) {
                Symbol.TypeSymbol typeSymbol = typeParameters.get(i);
                Type type = typeSymbol.type.getUpperBound();
                if (type == null) {
                    type = typeSymbol.type;
                }

                if (i > 0) sb.append(",");
                sb.append(ClassName.get(type));
            }

            if (sb.length() > 0) {
                sb.insert(0, "<").append(">");
                sb.insert(0, ClassName.get(e.asType()).toString().replaceAll("<.*>", ""));

                method.addStatement("$N.$N = ($N) $N.readValue(null)", object, e.getSimpleName(), sb.toString(), parcel);
            } else {
                method.addStatement("$N.$N = ($T) $N.readValue($T.class.getClassLoader())", object, e.getSimpleName(), e.asType(), parcel, e.asType());
            }
        } else {
            throw new IllegalStateException();
        }
    }

}
