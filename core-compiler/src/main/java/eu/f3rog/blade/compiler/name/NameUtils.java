package eu.f3rog.blade.compiler.name;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/**
 * Class {@link NameUtils}
 *
 * @author FrantisekGazo
 * @version 2016-04-10
 */
public class NameUtils {

    public static String toFQDN(TypeName className) {
        return className.toString();
    }

    public static String toFQDN(ClassName className) {
        return className.packageName() + "." + String.join(".", className.simpleNames());
    }

    public static String getNestedName(ClassName className) {
        return getNestedName(className, "_");
    }

    public static String getNestedName(ClassName className, String separator) {
        StringBuilder name = new StringBuilder();
        ClassName current = className;

        while (current != null) {
            if (current != className) {
                name.insert(0, separator);
            }
            name.insert(0, current.simpleName());

            current = current.enclosingClassName();
        }

        return name.toString();
    }

}
