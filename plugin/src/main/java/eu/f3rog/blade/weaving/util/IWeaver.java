package eu.f3rog.blade.weaving.util;

import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;

/**
 * Interface {@link IWeaver} used for bytecode weaving.
 *
 * @author FrantisekGazo
 */
public interface IWeaver {

    /**
     * Modify any class as you need :)
     */
    void weave(ClassPool classPool, List<CtClass> classes);

}

