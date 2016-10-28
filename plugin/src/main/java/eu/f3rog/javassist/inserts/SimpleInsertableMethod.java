package eu.f3rog.javassist.inserts;

import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import javassist.CtClass;

public final class SimpleInsertableMethod
        extends InsertableMethod {

    private final String mFullMethod;
    private final String mBody;
    private final String mTargetMethodName;
    private final CtClass[] mTargetMethodParams;
    private final InsertionPoint mInsertionPoint;

    public SimpleInsertableMethod(CtClass classToInsertInto,
                                  String targetMethodName, CtClass[] targetMethodParams,
                                  InsertionPoint insertionPoint,
                                  String body, String fullMethod) {
        super(classToInsertInto);
        this.mTargetMethodName = targetMethodName;
        this.mTargetMethodParams = targetMethodParams;
        this.mInsertionPoint = insertionPoint;
        this.mBody = body;
        this.mFullMethod = fullMethod;
    }

    @Override
    public InsertionPoint getInsertionPoint() {
        return mInsertionPoint;
    }

    @Override
    public String getFullMethod() throws AfterBurnerImpossibleException {
        return mFullMethod;
    }

    @Override
    public String getBody() throws AfterBurnerImpossibleException {
        return mBody;
    }

    @Override
    public String getTargetMethodName() throws AfterBurnerImpossibleException {
        return mTargetMethodName;
    }

    @Override
    public CtClass[] getTargetMethodParams() throws AfterBurnerImpossibleException {
        return mTargetMethodParams;
    }
}
