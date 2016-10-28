package eu.f3rog.javassist;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class Utils {

    public static CtMethod findTargetMethod(CtClass ctClass, String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
        CtMethod overriddenMethod = null;
        try {
            overriddenMethod = ctClass.getDeclaredMethod(targetMethod, targetMethodParams);
        } catch (Exception e) {
            for (CtMethod method : ctClass.getMethods()) {
                if (method.getName().equals(targetMethod) && equalArrays(method.getParameterTypes(), targetMethodParams)) {
                    overriddenMethod = method;
                    break;
                }
            }
        }
        if (overriddenMethod == null) {
            throw new NotFoundException(String.format("Class %s doesn't contain any method named %s", ctClass.getName(), targetMethod));
        }
        return overriddenMethod;
    }

    public static boolean equalArrays(CtClass[] a, CtClass[] a2) {
        if (a == a2) { // checks for same array reference
            return true;
        }
        if (a == null || a2 == null) { // checks for null arrays
            return false;
        }

        final int length = a.length;
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
