package eu.f3rog.javassist.inserts;

import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;

import javassist.CtClass;

/**
 * Base class of all insertable constructors through JavassistHelper.
 * Inserts code in all constructors of a target class. 
 * @author SNI
 */
public abstract class InsertableConstructor
        extends Insertable {

    public InsertableConstructor(CtClass classToInsertInto) {
        super(classToInsertInto);
    }

    /**
     * Return the list of java statements to be inserted in all constructors of the target class.
     * @param paramClasses the arguments passed to the constructor.
     * @return all instructions to be inserted at the beginning of each constructor.
     * @throws AfterBurnerImpossibleException in case something goes wrong. Wrap all exceptions into it.
     */
    public abstract String getConstructorBody(CtClass[] paramClasses) throws AfterBurnerImpossibleException;
    
    /**
     * Allows to filter constructors based on their parameter types.
     * @param paramClasses the arguments passed to the constructor.
     * @return whether or not the constructor will receive injected code (whether or not to call {@link #getConstructorBody(CtClass[])}).
     * @throws AfterBurnerImpossibleException in case something goes wrong. Wrap all exceptions into it.
     */
    public abstract boolean acceptParameters(CtClass[] paramClasses) throws AfterBurnerImpossibleException;

}
