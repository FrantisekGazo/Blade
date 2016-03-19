package eu.f3rog.blade.weaving.util;

import javassist.CtClass;

/**
 * Interface {@link IWeaver} used for bytecode weaving.
 *
 * @author FrantisekGazo
 * @version 2016-03-19
 */
public interface IWeaver {

    /**
     * Weave given helper class into given class.
     */
    void weave(CtClass helperClass, CtClass intoClass);

}

