package eu.f3rog.javassist;

import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import eu.f3rog.javassist.inserts.InsertableMethod;
import eu.f3rog.javassist.inserts.InsertionPoint;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import lombok.extern.slf4j.Slf4j;

/**
 * Injects code into a method.
 *
 * @author FrantisekGazo
 */
@Slf4j
final class InsertableMethodInjectorEditor
        extends ExprEditor {

    private final CtClass mClassToTransform;
    private final InsertionPoint mInsertionPoint;
    private final String mBodyToInsert;
    private boolean mIsSuccessful;

    public InsertableMethodInjectorEditor(CtClass classToTransform, InsertableMethod insertableMethod)
            throws AfterBurnerImpossibleException {
        this.mClassToTransform = classToTransform;
        this.mInsertionPoint = insertableMethod.getInsertionPoint();
        this.mBodyToInsert = insertableMethod.getBody();
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
        if (m.getMethodName().equals(mInsertionPoint.getCall())) {

            String origMethodCall = "$_ = $proceed($$);;\n";

            switch (mInsertionPoint.getType()) {
                case BEFORE:
                    origMethodCall = mBodyToInsert + origMethodCall;
                    break;
                case AFTER:
                    origMethodCall = origMethodCall + mBodyToInsert;
                    break;
                default:
                    throw new IllegalStateException("Unsupported insertion type " + mInsertionPoint.getType());
            }

            log.info("Injected : " + origMethodCall);
            log.info("Class " + mClassToTransform.getName() + " has been enhanced.");
            m.replace(origMethodCall);
            mIsSuccessful = true;
        }
    }

    public void edit(CtMethod targetMethod) throws CannotCompileException {
        switch (mInsertionPoint.getType()) {
            case BEFORE:
            case AFTER:
                targetMethod.instrument(this);
                if (!mIsSuccessful) {
                    throw new CannotCompileException("Transformation failed. Insertion method not found.: " + targetMethod.getName());
                }
                break;
            case BEFORE_BODY:
                targetMethod.insertBefore(mBodyToInsert);
                break;
            case AFTER_BODY:
                targetMethod.insertAfter(mBodyToInsert);
                break;
        }
    }
}