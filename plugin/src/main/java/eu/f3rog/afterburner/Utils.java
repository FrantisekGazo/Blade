package eu.f3rog.afterburner;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Class {@link Utils}
 *
 * @author FrantisekGazo
 * @version 2016-01-02
 */
public class Utils {

    private Utils() {}

    public static CtMethod findTargetMethod(CtClass ctClass, String targetMethod, CtClass... targetMethodParams) {
        CtMethod overriddenMethod = null;
        try {
            overriddenMethod = ctClass.getDeclaredMethod(targetMethod, targetMethodParams);
        } catch (Exception e) {
            for (CtMethod method : ctClass.getMethods()) {
                CtClass[] params;
                try {
                    params = method.getParameterTypes();
                } catch (NotFoundException nfe) {
                    continue;
                }
                if (method.getName().equals(targetMethod)
                        && compareArrays(params, targetMethodParams)) {
                    overriddenMethod = method;
                    break;
                }
            }
        }
        return overriddenMethod;
    }

    private static boolean compareArrays(CtClass[] a, CtClass[] a2) {
        if (a == a2) { // checks for same array reference
            return true;
        }
        if (a == null || a2 == null) { // checks for null arrays
            return false;
        }

        int length = a.length;
        if (a2.length != length) { // arrays should be of equal length
            return false;
        }

        for (int i = 0; i < length; i++) { // compare array values
            if (!a[i].equals(a2[i])) {
                return false;
            }
        }

        return true;
    }

}
