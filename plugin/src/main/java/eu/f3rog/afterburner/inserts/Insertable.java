package eu.f3rog.afterburner.inserts;

import javassist.CtClass;

/**
 * Base class of all insertable pieces of code through AfterBurner. 
 * @author SNI
 */
public class Insertable {

    /** The target class into which to insert code. */
    private CtClass classToInsertInto;

    public Insertable(CtClass classToInsertInto) {
        this.classToInsertInto = classToInsertInto;
    }
    
    public CtClass getClassToInsertInto() {
        return classToInsertInto;
    }

}
