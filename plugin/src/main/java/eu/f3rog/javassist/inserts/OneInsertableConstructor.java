package eu.f3rog.javassist.inserts;

import eu.f3rog.javassist.Utils;
import javassist.CtClass;

/**
 * Matches one specific class constructor.
 *
 * @author FrantisekGazo
 */
public final class OneInsertableConstructor
        extends InsertableConstructor {

    private final CtClass[] mRequiredParams;
    private final String mBody;

    public OneInsertableConstructor(String body, CtClass classToInsertInto, CtClass... params) {
        super(classToInsertInto);
        mRequiredParams = params;
        mBody = body;
    }

    @Override
    public String getConstructorBody(CtClass[] paramClasses) throws eu.f3rog.javassist.exception.AfterBurnerImpossibleException {
        return mBody;
    }

    @Override
    public boolean acceptParameters(CtClass[] paramClasses) throws eu.f3rog.javassist.exception.AfterBurnerImpossibleException {
        return Utils.equalArrays(mRequiredParams, paramClasses);
    }
}