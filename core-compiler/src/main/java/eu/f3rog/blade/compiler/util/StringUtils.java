package eu.f3rog.blade.compiler.util;

/**
 * Class {@link StringUtils}
 *
 * @author FrantisekGazo
 * @version 2015-10-18
 */
public class StringUtils {

    private StringUtils() {}

    public static String startLowerCase(final String className) {
        return className.substring(0, 1).toLowerCase()
                + className.substring(1);
    }

    public static String startUpperCase(final String className) {
        return className.substring(0, 1).toUpperCase()
                + className.substring(1);
    }

}
