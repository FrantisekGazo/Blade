package eu.f3rog.javassist.inserts;

import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import javassist.CtClass;

/**
 * Matches all of class constructors.
 *
 * @author FrantisekGazo
 */
public final class AllInsertableConstructor
        extends InsertableConstructor {

    private final String mBody;

    public AllInsertableConstructor(String body, CtClass classToInsertInto) {
        super(classToInsertInto);
        this.mBody = body;
    }

    @Override
    public String getConstructorBody(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
        return mBody;
    }

    @Override
    public boolean acceptParameters(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
        return true;
    }
}
