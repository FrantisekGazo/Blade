package eu.f3rog.blade.weaving.interfaces;

import java.util.HashMap;
import java.util.Map;

import eu.f3rog.javassist.JavassistHelper;
import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Weaves interfaces into classes.
 *
 * @author FrantisekGazo
 */
public final class Interfaces {

    private Interfaces() {
    }

    private static Map<String, InterfaceWeaver> sSupportedInterfaces = null;

    /**
     * Weaves given interfaceClass into targetClass.
     */
    public static void weaveInterface(CtClass interfaceClass,
                                      CtClass targetClass,
                                      JavassistHelper javassistHelper)
            throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException {

        if (sSupportedInterfaces == null) {
            initSupportedInterfaces();
        }

        InterfaceWeaver weaver = sSupportedInterfaces.get(interfaceClass.getName());
        if (weaver == null) {
            throw new IllegalArgumentException("Interface not supported");
        }
        weaver.weave(interfaceClass, targetClass, javassistHelper);
    }

    private static void initSupportedInterfaces() {
        sSupportedInterfaces = new HashMap<>();

        sSupportedInterfaces.put("eu.f3rog.blade.mvp.WeavedMvpActivity", new WeavedMvpActivityIW());
        sSupportedInterfaces.put("eu.f3rog.blade.mvp.WeavedMvpFragment", new WeavedMvpFragmentIW());
        sSupportedInterfaces.put("eu.f3rog.blade.mvp.WeavedMvpView", new WeavedMvpViewIW());
    }
}
