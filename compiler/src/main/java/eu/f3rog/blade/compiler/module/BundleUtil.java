package eu.f3rog.blade.compiler.module;

import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.lang.model.element.VariableElement;

/**
 * Class {@link BundleUtil}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class BundleUtil {

    public static void getFromBundle(MethodSpec.Builder method,
                                     String targetName,
                                     List<VariableElement> fields,
                                     String format,
                                     String bundleName) {
        for (int i = 0; i < fields.size(); i++) {
            VariableElement field = fields.get(i);
            method.addStatement("$N.$N = $N.get($S, $N.$N)",
                    targetName, field.getSimpleName(),
                    bundleName, String.format(format, field.getSimpleName().toString()),
                    targetName, field.getSimpleName());
        }
    }

    public static void putToBundle(MethodSpec.Builder method,
                                   String targetName,
                                   List<VariableElement> fields,
                                   String format,
                                   String bundleName) {
        for (int i = 0; i < fields.size(); i++) {
            VariableElement field = fields.get(i);
            method.addStatement("$N.put($S, $N.$N)",
                    bundleName, String.format(format, field.getSimpleName().toString()),
                    targetName, field.getSimpleName());
        }
    }

}
