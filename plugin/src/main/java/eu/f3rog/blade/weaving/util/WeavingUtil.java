package eu.f3rog.blade.weaving.util;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;

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

    public static boolean implementsInterface(CtClass cls, CtClass interfaceClass) throws NotFoundException {
        CtClass[] interfaces = cls.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(interfaceClass)) {
                return true;
            }
        }
        return false;
    }

    public static AnnotationsAttribute getAnnotations(CtClass ctClass) {
        return (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
    }

    public static AnnotationsAttribute getAnnotations(CtMethod ctMethod) {
        return (AnnotationsAttribute) ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
    }

}
