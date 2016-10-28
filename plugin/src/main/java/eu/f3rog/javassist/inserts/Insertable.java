package eu.f3rog.javassist.inserts;

import javassist.CtClass;

/**
 * Base class of all insertable pieces of code through JavassistHelper.
 * @author SNI
 */
public abstract class Insertable {

    /** The target class into which to insert code. */
    private CtClass classToInsertInto;

    public Insertable(CtClass classToInsertInto) {
        this.classToInsertInto = classToInsertInto;
    }
    
    public CtClass getClassToInsertInto() {
        return classToInsertInto;
    }

}
