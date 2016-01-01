package eu.f3rog.afterburner.inserts;

import eu.f3rog.afterburner.exception.AfterBurnerImpossibleException;

import javassist.CtClass;

public final class SimpleInsertableConstructor extends InsertableConstructor {

    private String body;
    private boolean acceptParameters;

    public SimpleInsertableConstructor(CtClass classToInsertInto, String body, boolean acceptParameters) {
        super(classToInsertInto);
        this.body = body;
        this.acceptParameters = acceptParameters;
    }

    @Override
    public String getConstructorBody(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
        return body;
    }

    @Override
    public boolean acceptParameters(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
        return acceptParameters;
    }
}
