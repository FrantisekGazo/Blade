package eu.f3rog.javassist;

import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import eu.f3rog.javassist.inserts.CtMethodJavaWriter;
import eu.f3rog.javassist.inserts.InsertableMethod;
import eu.f3rog.javassist.inserts.InsertionPoint;
import eu.f3rog.javassist.inserts.InsertionType;
import eu.f3rog.javassist.inserts.SimpleInsertableMethod;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Almost a DSL/builder to ease creating an {@link eu.f3rog.javassist.inserts.InsertableMethod}. Needs more
 * intermediate states.
 *
 * @author SNI and FrantisekGazo
 */
@Slf4j
public final class InsertableMethodBuilder {

    private CtClass mClassToInsertInto;
    private String mTargetMethod;
    private CtClass[] mTargetMethodParams;
    protected String mFullMethod;
    protected String mBody;
    protected InsertionPoint mInsertionPoint;

    private final CtMethodJavaWriter mSignatureExtractor;

    public InsertableMethodBuilder(CtMethodJavaWriter signatureExtractor) {
        this.mSignatureExtractor = signatureExtractor;
    }

    public StateTargetClassSet insertIntoClass(Class<?> clazzToInsertInto) throws NotFoundException {
        this.mClassToInsertInto = ClassPool.getDefault().get(clazzToInsertInto.getName());
        return new StateTargetClassSet();
    }

    public StateTargetClassSet insertIntoClass(CtClass classToInsertInto) {
        this.mClassToInsertInto = classToInsertInto;
        return new StateTargetClassSet();
    }

    private void doInsertBodyInFullMethod() {
        if (mFullMethod != null) {
            if (mFullMethod.contains(InsertableMethod.BODY_TAG)) {
                mFullMethod = mFullMethod.replace(InsertableMethod.BODY_TAG, mBody);
            } else {
                log.info("Full method doesn't contain body tag (InsertableMethod.BODY_TAG=" + InsertableMethod.BODY_TAG + ")");
            }
        }
    }

    protected void checkFields() throws AfterBurnerImpossibleException {
        if (mClassToInsertInto == null || mTargetMethod == null || mBody == null || mFullMethod == null) {
            throw new AfterBurnerImpossibleException("Builder was not used as intended. A field is null.");
        }
    }

    //**********************************************
    //******* FLUENT DSL STATE CLASSES
    //**********************************************


    public final class StateTargetClassSet {

        public StateTargetMethodSet inMethodIfExists(String targetMethod, CtClass... targetMethodParams) {
            InsertableMethodBuilder.this.mTargetMethod = targetMethod;
            InsertableMethodBuilder.this.mTargetMethodParams = targetMethodParams;
            return new StateTargetMethodSet();
        }

        public StateInsertionPointAndFullMethodSet beforeBody(String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
            InsertableMethodBuilder.this.mTargetMethod = targetMethod;
            InsertableMethodBuilder.this.mTargetMethodParams = targetMethodParams;
            InsertableMethodBuilder.this.mInsertionPoint = new InsertionPoint(InsertionType.BEFORE_BODY);
            CtMethod overridenMethod = Utils.findTargetMethod(mClassToInsertInto, targetMethod, targetMethodParams);
            mFullMethod = mSignatureExtractor.createJavaSignature(overridenMethod)
                    + " { \n"
                    + InsertableMethod.BODY_TAG
                    + "\n"
                    + ((!overridenMethod.getReturnType().toString().equals("void")) ? "return " : "")
                    + mSignatureExtractor.createSuperCall(overridenMethod) + "}\n";
            log.info("Creating override " + mFullMethod);
            return new StateInsertionPointAndFullMethodSet();
        }

        public StateInsertionPointAndFullMethodSet afterBody(String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
            InsertableMethodBuilder.this.mTargetMethod = targetMethod;
            InsertableMethodBuilder.this.mTargetMethodParams = targetMethodParams;
            InsertableMethodBuilder.this.mInsertionPoint = new InsertionPoint(InsertionType.AFTER_BODY);
            CtMethod overridenMethod = Utils.findTargetMethod(mClassToInsertInto, targetMethod, targetMethodParams);
            mFullMethod = mSignatureExtractor.createJavaSignature(overridenMethod)
                    + " { \n"
                    + InsertableMethod.BODY_TAG
                    + "\n"
                    + ((!overridenMethod.getReturnType().toString().equals("void")) ? "return " : "")
                    + mSignatureExtractor.createSuperCall(overridenMethod) + "}\n";
            log.info("Creating override " + mFullMethod);
            return new StateInsertionPointAndFullMethodSet();
        }

        public StateInsertionPointAndFullMethodSet beforeSuper(String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
            InsertableMethodBuilder.this.mTargetMethod = targetMethod;
            InsertableMethodBuilder.this.mTargetMethodParams = targetMethodParams;
            InsertableMethodBuilder.this.mInsertionPoint = new InsertionPoint(InsertionType.BEFORE, targetMethod);
            CtMethod overridenMethod = Utils.findTargetMethod(mClassToInsertInto, targetMethod, targetMethodParams);
            mFullMethod = mSignatureExtractor.createJavaSignature(overridenMethod)
                    + " { \n"
                    + InsertableMethod.BODY_TAG
                    + "\n"
                    + mSignatureExtractor.createSuperCall(overridenMethod) + "}\n";
            log.info("Creating override " + mFullMethod);
            return new StateInsertionPointAndFullMethodSet();
        }

        public StateInsertionPointAndFullMethodSet afterSuper(String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
            InsertableMethodBuilder.this.mTargetMethod = targetMethod;
            InsertableMethodBuilder.this.mTargetMethodParams = targetMethodParams;
            InsertableMethodBuilder.this.mInsertionPoint = new InsertionPoint(InsertionType.AFTER, targetMethod);
            CtMethod overridenMethod = Utils.findTargetMethod(mClassToInsertInto, targetMethod, targetMethodParams);
            mFullMethod = mSignatureExtractor.createJavaSignature(overridenMethod)
                    + " { \n"
                    + mSignatureExtractor.createSuperCall(overridenMethod)
                    + "\n"
                    + InsertableMethod.BODY_TAG + "}\n";
            log.info("Creating override " + mFullMethod);
            return new StateInsertionPointAndFullMethodSet();
        }
    }

    public final class StateTargetMethodSet {

        public StateInsertionPointSet beforeCallTo(String insertionBeforeMethod) {
            InsertableMethodBuilder.this.mInsertionPoint = new InsertionPoint(InsertionType.BEFORE, insertionBeforeMethod);
            return new StateInsertionPointSet();
        }

        public StateInsertionPointSet afterCallTo(String insertionAfterMethod) {
            InsertableMethodBuilder.this.mInsertionPoint = new InsertionPoint(InsertionType.AFTER, insertionAfterMethod);
            return new StateInsertionPointSet();
        }
    }

    public final class StateInsertionPointSet {

        public StateBodySet withBody(String body) {
            InsertableMethodBuilder.this.mBody = body;
            return new StateBodySet();
        }
    }

    public final class StateInsertionPointAndFullMethodSet {

        public StateComplete withBody(String body) {
            InsertableMethodBuilder.this.mBody = body;
            return new StateComplete();
        }
    }

    public final class StateBodySet {

        public StateComplete elseCreateMethodIfNotExists(String fullMethod) {
            InsertableMethodBuilder.this.mFullMethod = fullMethod;
            return new StateComplete();
        }
    }

    public final class StateComplete {

        public InsertableMethod createInsertableMethod() throws AfterBurnerImpossibleException {
            checkFields();
            doInsertBodyInFullMethod();

            return new SimpleInsertableMethod(
                    mClassToInsertInto,
                    mTargetMethod, mTargetMethodParams,
                    mInsertionPoint,
                    mBody, mFullMethod
            );
        }
    }

}
