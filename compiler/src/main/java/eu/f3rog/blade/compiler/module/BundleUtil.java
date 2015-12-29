package eu.f3rog.blade.compiler.module;

import com.squareup.javapoet.MethodSpec;

import java.util.List;

/**
 * Class {@link BundleUtil}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class BundleUtil {

    public static void getFromBundle(MethodSpec.Builder method,
                                     String targetName,
                                     List<String> fields,
                                     String format,
                                     String bundleName) {
        for (int i = 0; i < fields.size(); i++) {
            String fieldName = fields.get(i);
            method.addStatement("$N.$N = $N.get($S, $N.$N)",
                    targetName, fieldName,
                    bundleName, String.format(format, fieldName),
                    targetName, fieldName);
        }
    }

    public static void putToBundle(MethodSpec.Builder method,
                                   String targetName,
                                   List<String> fields,
                                   String format,
                                   String bundleName) {
        for (int i = 0; i < fields.size(); i++) {
            String fieldName = fields.get(i);
            method.addStatement("$N.put($S, $N.$N)",
                    bundleName, String.format(format, fieldName),
                    targetName, fieldName);
        }
    }

}
