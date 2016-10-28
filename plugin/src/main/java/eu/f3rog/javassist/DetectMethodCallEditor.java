package eu.f3rog.javassist;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

final class DetectMethodCallEditor
        extends ExprEditor {

    private final CtMethod mWithinMethod;
    private final String mMethodName;

    private boolean mIsCallingMethod;

    public DetectMethodCallEditor(CtMethod withinMethod, String methodName) {
        this.mWithinMethod = withinMethod;
        this.mMethodName = methodName;
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
        if (m.getMethodName().equals(mMethodName)) {
            this.mIsCallingMethod = true;
        }
    }

    public boolean checkIfisCallingMethod() throws CannotCompileException {
        mWithinMethod.instrument(this);
        return mIsCallingMethod;
    }
}
