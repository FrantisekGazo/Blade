package eu.f3rog.blade.weaving.util;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

/**
 * Class {@link WeavingUtil}
 *
 * @author FrantisekGazo
 * @version 2015-11-09
 */
public final class WeavingUtil {

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

    public static boolean hasAnnotation(final CtClass targetClass, final String... annotationClassNames) {
        final AnnotationsAttribute annotations = getAnnotations(targetClass);
        for (final String annotationClassName : annotationClassNames) {
            final Annotation a = annotations.getAnnotation(annotationClassName);
            if (a != null) {
                return true;
            }
        }
        return false;
    }

    public static AnnotationsAttribute getAnnotations(final CtClass ctClass) {
        return (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
    }

    public static AnnotationsAttribute getAnnotations(final CtMethod ctMethod) {
        return (AnnotationsAttribute) ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
    }

    public static AnnotationsAttribute getAnnotations(final CtField ctField) {
        return (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
    }

}
