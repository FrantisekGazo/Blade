package eu.f3rog.blade.weaving.interfaces;

import eu.f3rog.javassist.JavassistHelper;
import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;


/**
 * Base class for weaving an interface
 *
 * @author FrantisekGazo
 */
abstract class InterfaceWeaver {

    /**
     * Weaves given interfaceClass into targetClass.
     */
    abstract void weave(CtClass interfaceClass, CtClass targetClass, JavassistHelper javassistHelper)
            throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException;

    protected boolean willBeImplementedBySuperclass(CtClass targetClass, CtClass interfaceClass) throws NotFoundException {
        ClassPool classPool = targetClass.getClassPool();
        CtClass currentClass = targetClass.getSuperclass();

        while (currentClass != null) {
            String className = currentClass.getName();
            if (!className.contains("_Helper")) {
                try {
                    CtClass helperClass = classPool.get(className + "_Helper");
                    if (helperClass.subtypeOf(interfaceClass)) {
                        return true;
                    }
                } catch (NotFoundException e) {
                    // do nothing if helper does not exists
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return false;
    }
}
