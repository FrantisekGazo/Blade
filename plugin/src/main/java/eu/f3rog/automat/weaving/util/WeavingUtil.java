package eu.f3rog.automat.weaving.util;

import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Class {@link WeavingUtil}
 *
 * @author FrantisekGazo
 * @version 2015-11-09
 */
public class WeavingUtil {

    public static boolean isSubclassOf(CtClass clazz, String superClassName) throws NotFoundException {
        CtClass superClass = clazz;

        do {
            //System.out.printf("isSubclassOf %s : %s\n", superClassName, superClass.getName());
            if (superClass.getName().equals(superClassName)) return true;
            superClass = superClass.getSuperclass();
        } while (superClass != null);

        return false;

    }

    public static boolean isSubclassOf(CtClass clazz, String... superClassNames) throws NotFoundException {
        CtClass superClass = clazz;

        while (superClass != null) {
            for (int i = 0; i < superClassNames.length; i++) {
                if (superClass.getName().equals(superClassNames[i])) return true;
            }
            superClass = superClass.getSuperclass();
        }

        return false;

    }

}
