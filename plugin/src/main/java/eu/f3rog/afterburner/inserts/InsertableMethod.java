package eu.f3rog.afterburner.inserts;

import eu.f3rog.afterburner.exception.AfterBurnerImpossibleException;
import javassist.CtClass;

/**
 * Base class of all insertable methods through AfterBurner.
 * Inserts code into a given method. It will inject code using an "insertion point", i.e.
 * a method call inside the target method, either before or after it.
 * If there is no method to insert into, fully create the target method.
 * Due to limitations in javassist (https://github.com/jboss-javassist/javassist/issues/9),
 * one of the overloads of the target method is chosen arbitrarily to insert code.
 *
 * @author SNI
 */
public abstract class InsertableMethod extends Insertable {

    public static final String BODY_TAG = "==BODY==";

    public InsertableMethod(CtClass classToInsertInto) {
        super(classToInsertInto);
    }

    public String getInsertionBeforeMethod() {
        return null;
    }

    public String getInsertionAfterMethod() {
        return null;
    }

    /**
     * Return the full method (signature + body) to add to the classToInsertInto.
     * A special mechanism allow to replace the tag #BODY_TAG by the result of #getBody().
     *
     * @return the full method (signature + body) to add to the classToInsertInto.
     * @throws AfterBurnerImpossibleException in case something goes wrong. Wrap all exceptions into it.
     */
    public abstract String getFullMethod() throws AfterBurnerImpossibleException;

    /**
     * Return the java statements to insert.
     *
     * @return the instructions (no signature) to insert;
     * @throws AfterBurnerImpossibleException in case something goes wrong. Wrap all exceptions into it.
     */
    public abstract String getBody() throws AfterBurnerImpossibleException;

    /**
     * Return the name of the method to insert code into.
     *
     * @return the name of the method to insert code into.
     * @throws AfterBurnerImpossibleException in case something goes wrong. Wrap all exceptions into it.
     */
    public abstract String getTargetMethodName() throws AfterBurnerImpossibleException;

    public abstract CtClass[] getTargetMethodParams() throws AfterBurnerImpossibleException;

    public String toString() {
        String fullMethod;
        try {
            fullMethod = getFullMethod();
        } catch (AfterBurnerImpossibleException e) {
            fullMethod = "<Exception>";
        }
        String body;
        try {
            body = getBody();
        } catch (AfterBurnerImpossibleException e) {
            body = "<Exception>";
        }
        final String string = "[class:"
                + getClassToInsertInto().getName()
                + ",before:"
                + getInsertionBeforeMethod()
                + ",after:"
                + getInsertionAfterMethod()
                + ",fullMethod:"
                + fullMethod
                + ",body:"
                + body
                + "]";
        return string;
    }
}
