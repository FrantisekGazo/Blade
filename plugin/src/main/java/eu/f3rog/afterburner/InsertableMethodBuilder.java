package eu.f3rog.afterburner;

import eu.f3rog.afterburner.exception.AfterBurnerImpossibleException;
import eu.f3rog.afterburner.inserts.CtMethodJavaWriter;
import eu.f3rog.afterburner.inserts.InsertableMethod;
import eu.f3rog.afterburner.inserts.SimpleInsertableMethod;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Almost a DSL/builder to ease creating an {@link InsertableMethod}. Needs more
 * intermediate states.
 *
 * @author SNI
 */
@Slf4j
public class InsertableMethodBuilder {

    private String targetMethod;
    private CtClass[] targetMethodParams;
    private CtClass classToInsertInto;
    protected String fullMethod;
    protected String body;
    protected String insertionBeforeMethod;
    protected String insertionAfterMethod;
    private AfterBurner afterBurner;
    private CtMethodJavaWriter signatureExtractor;

    public InsertableMethodBuilder(AfterBurner afterBurner) {
        this(afterBurner, null);
    }

    public InsertableMethodBuilder(AfterBurner afterBurner,
                                   CtMethodJavaWriter signatureExtractor) {
        this.afterBurner = afterBurner;
        this.signatureExtractor = signatureExtractor;
    }

    public StateTargetClassSet insertIntoClass(Class<?> clazzToInsertInto) throws NotFoundException {
        this.classToInsertInto = ClassPool.getDefault().get(
                clazzToInsertInto.getName());
        return new StateTargetClassSet();
    }

    public StateTargetClassSet insertIntoClass(CtClass classToInsertInto) {
        this.classToInsertInto = classToInsertInto;
        return new StateTargetClassSet();
    }

    protected void checkFields() throws AfterBurnerImpossibleException {
        if (classToInsertInto == null || targetMethod == null
                || body == null || fullMethod == null) {
            throw new AfterBurnerImpossibleException(
                    "Builder was not used as intended. A field is null.");
        }
    }

    //**********************************************
    //******* FLUENT DSL STATE CLASSES
    //**********************************************


    public class StateTargetClassSet {

        public static final String CLASS_S_DOESN_T_CONTAIN_ANY_METHOD_NAMED_S = "Class %s doesn't contain any method named %s";
        public static final String OPENING_BRACE_NEW_LINE_CHARACTER = " { \n";
        public static final String CREATING_OVERRIDE = "Creating override ";

        public StateTargetMethodSet inMethodIfExists(String targetMethod, CtClass... targetMethodParams) {
            InsertableMethodBuilder.this.targetMethod = targetMethod;
            InsertableMethodBuilder.this.targetMethodParams = targetMethodParams;
            return new StateTargetMethodSet();
        }

        public StateInsertionPointAndFullMethodSet atBeginningOfOverrideMethod(String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
            InsertableMethodBuilder.this.targetMethod = targetMethod;
            InsertableMethodBuilder.this.targetMethodParams = targetMethodParams;
            // set no insertion method ! (it will be insert at the beginning)
            CtMethod overridenMethod = findTargetMethod(targetMethod, targetMethodParams);
            if (overridenMethod == null) {
                throw new NotFoundException(String.format(CLASS_S_DOESN_T_CONTAIN_ANY_METHOD_NAMED_S, classToInsertInto.getName(), targetMethod));
            }
            fullMethod = signatureExtractor
                    .createJavaSignature(overridenMethod)
                    + OPENING_BRACE_NEW_LINE_CHARACTER
                    + InsertableMethod.BODY_TAG
                    + "\n"
                    + ((!overridenMethod.getReturnType().toString().equals("void")) ? "return " : "")
                    + signatureExtractor.invokeSuper(overridenMethod) + "}\n";
            log.info(CREATING_OVERRIDE + fullMethod);
            return new StateInsertionPointAndFullMethodSet();
        }

        public StateInsertionPointAndFullMethodSet beforeOverrideMethod(String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
            InsertableMethodBuilder.this.targetMethod = targetMethod;
            InsertableMethodBuilder.this.targetMethodParams = targetMethodParams;
            InsertableMethodBuilder.this.insertionBeforeMethod = targetMethod;
            CtMethod overridenMethod = findTargetMethod(targetMethod, targetMethodParams);
            if (overridenMethod == null) {
                throw new NotFoundException(String.format(CLASS_S_DOESN_T_CONTAIN_ANY_METHOD_NAMED_S, classToInsertInto.getName(), targetMethod));
            }
            fullMethod = signatureExtractor
                    .createJavaSignature(overridenMethod)
                    + OPENING_BRACE_NEW_LINE_CHARACTER
                    + InsertableMethod.BODY_TAG
                    + "\n"
                    + signatureExtractor.invokeSuper(overridenMethod) + "}\n";
            log.info(CREATING_OVERRIDE + fullMethod);
            return new StateInsertionPointAndFullMethodSet();
        }

        public StateInsertionPointAndFullMethodSet afterOverrideMethod(String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
            InsertableMethodBuilder.this.targetMethod = targetMethod;
            InsertableMethodBuilder.this.insertionAfterMethod = targetMethod;
            CtMethod overridenMethod = findTargetMethod(targetMethod, targetMethodParams);
            if (overridenMethod == null) {
                throw new NotFoundException(String.format(CLASS_S_DOESN_T_CONTAIN_ANY_METHOD_NAMED_S, classToInsertInto.getName(), targetMethod));
            }
            fullMethod = signatureExtractor
                    .createJavaSignature(overridenMethod)
                    + OPENING_BRACE_NEW_LINE_CHARACTER
                    + signatureExtractor.invokeSuper(overridenMethod)
                    + "\n"
                    + InsertableMethod.BODY_TAG + "}\n";
            log.info(CREATING_OVERRIDE + fullMethod);
            return new StateInsertionPointAndFullMethodSet();
        }

        private CtMethod findTargetMethod(String targetMethod, CtClass... targetMethodParams) throws NotFoundException {
            CtMethod overriddenMethod = null;
            try {
                overriddenMethod = classToInsertInto.getDeclaredMethod(targetMethod, targetMethodParams);
            } catch (Exception e) {
                for (CtMethod method : classToInsertInto.getMethods()) {
                    if (method.getName().equals(targetMethod)
                            && compareArrays(method.getParameterTypes(), targetMethodParams)) {
                        overriddenMethod = method;
                        break;
                    }
                }
            }
            return overriddenMethod;
        }

        private boolean compareArrays(CtClass[] a, CtClass[] a2) {
            if (a == a2) { // checks for same array reference
                return true;
            }
            if (a == null || a2 == null) { // checks for null arrays
                return false;
            }

            int length = a.length;
            if (a2.length != length) { // arrays should be of equal length
                return false;
            }

            for (int i = 0; i < length; i++) { // compare array values
                if (!a[i].equals(a2[i])) {
                    return false;
                }
            }

            return true;
        }
    }

    public class StateTargetMethodSet {
        public StateInsertionPointSet beforeACallTo(String insertionBeforeMethod) {
            InsertableMethodBuilder.this.insertionBeforeMethod = insertionBeforeMethod;
            return new StateInsertionPointSet();
        }

        public StateInsertionPointSet afterACallTo(String insertionAfterMethod) {
            InsertableMethodBuilder.this.insertionAfterMethod = insertionAfterMethod;
            return new StateInsertionPointSet();
        }
    }

    public class StateInsertionPointSet {
        public StateBodySet withBody(String body) {
            InsertableMethodBuilder.this.body = body;
            return new StateBodySet();
        }
    }

    public class StateInsertionPointAndFullMethodSet {
        public StateComplete withBody(String body) {
            InsertableMethodBuilder.this.body = body;
            return new StateComplete();
        }
    }

    public class StateBodySet {
        public StateComplete elseCreateMethodIfNotExists(String fullMethod) {
            InsertableMethodBuilder.this.fullMethod = fullMethod;
            return new StateComplete();
        }
    }

    public class StateComplete {

        private void doInsertBodyInFullMethod() {
            if (fullMethod != null) {
                if (!fullMethod.contains(InsertableMethod.BODY_TAG)) {
                    log.info("Full method doesn't contain body tag (InsertableMethod.BODY_TAG=" + InsertableMethod.BODY_TAG + ")");
                }
                fullMethod = fullMethod.replace(InsertableMethod.BODY_TAG, body);
            }
        }

        public InsertableMethod createInsertableMethod() throws AfterBurnerImpossibleException {
            checkFields();
            doInsertBodyInFullMethod();

            InsertableMethod method = new SimpleInsertableMethod(
                    classToInsertInto, targetMethod, targetMethodParams,
                    insertionBeforeMethod, insertionAfterMethod,
                    body, fullMethod);
            return method;
        }

        public void doIt() throws CannotCompileException,
                AfterBurnerImpossibleException {
            InsertableMethod method = createInsertableMethod();
            afterBurner.addOrInsertMethod(method);
        }
    }

}
