package eu.f3rog.javassist;

import java.util.ArrayList;
import java.util.List;

import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import eu.f3rog.javassist.inserts.CtMethodJavaWriter;
import eu.f3rog.javassist.inserts.InsertableConstructor;
import eu.f3rog.javassist.inserts.InsertableMethod;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Allows to modify byte code of java classes via javassist.
 * This class allows a rich API to injeect byte code into methods or constructors of a given class.
 *
 * @author SNI and FrantisekGazo
 */
@Slf4j
public final class JavassistHelper {

    private final CtMethodJavaWriter mSignatureExtractor;

    public JavassistHelper() {
        mSignatureExtractor = new CtMethodJavaWriter();
    }

    /**
     * Add/Inserts java instructions into a given method of a given class.
     *
     * @param insertableMethod contains all information to perform byte code injection.
     * @throws CannotCompileException         if the source contained in insertableMethod can't be compiled.
     * @throws AfterBurnerImpossibleException if something else goes wrong, wraps other exceptions.
     */
    public void addOrInsertMethod(InsertableMethod insertableMethod)
            throws CannotCompileException, AfterBurnerImpossibleException {
        log.info("InsertableMethod : " + insertableMethod);
        CtClass classToTransform = insertableMethod.getClassToInsertInto();
        String targetMethodName = insertableMethod.getTargetMethodName();
        CtClass[] targetMethodParams = insertableMethod.getTargetMethodParams();
        CtMethod targetMethod = extractExistingMethod(classToTransform, targetMethodName, targetMethodParams);

        log.info("Method : " + targetMethod);
        if (targetMethod != null) {
            InsertableMethodInjectorEditor injectorEditor = new InsertableMethodInjectorEditor(classToTransform, insertableMethod);
            injectorEditor.edit(targetMethod);
        } else {
            CtMethod method = CtNewMethod.make(insertableMethod.getFullMethod(), classToTransform);
            classToTransform.addMethod(method);
        }
    }

    public void insertBeforeBody(String body, CtClass targetClass, String targetMethodName, CtClass... targetMethodParams)
            throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableMethod insertableMethod = new InsertableMethodBuilder(mSignatureExtractor)
                .insertIntoClass(targetClass)
                .beforeBody(targetMethodName, targetMethodParams)
                .withBody(body)
                .createInsertableMethod();
        addOrInsertMethod(insertableMethod);
    }

    public void insertAfterBody(String body, CtClass targetClass, String targetMethodName, CtClass... targetMethodParams)
            throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableMethod insertableMethod = new InsertableMethodBuilder(mSignatureExtractor)
                .insertIntoClass(targetClass)
                .afterBody(targetMethodName, targetMethodParams)
                .withBody(body)
                .createInsertableMethod();
        addOrInsertMethod(insertableMethod);
    }

    /**
     * Add/Inserts java instructions into a given override method of a given class. Before the overriden method call.
     *
     * @param targetClass      the class to inject code into.
     * @param targetMethodName the method to inject code into. Body will be injected right before the call to super.&lt;targetName&gt;.
     * @param body             the instructions of java to be injected.
     * @throws CannotCompileException         if the source contained in insertableMethod can't be compiled.
     * @throws AfterBurnerImpossibleException if something else goes wrong, wraps other exceptions.
     */
    public void insertBeforeSuper(String body, CtClass targetClass, String targetMethodName, CtClass... targetMethodParams)
            throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableMethod insertableMethod = new InsertableMethodBuilder(mSignatureExtractor)
                .insertIntoClass(targetClass)
                .beforeSuper(targetMethodName, targetMethodParams)
                .withBody(body)
                .createInsertableMethod();
        addOrInsertMethod(insertableMethod);
    }

    /**
     * Add/Inserts java instructions into a given override method of a given class. After the overriden method call.
     *
     * @param targetClass      the class to inject code into.
     * @param targetMethodName the method to inject code into. Body will be injected right after the call to super.&lt;targetName&gt;.
     * @param body             the instructions of java to be injected.
     * @throws CannotCompileException         if the source contained in insertableMethod can't be compiled.
     * @throws AfterBurnerImpossibleException if something else goes wrong, wraps other exceptions.
     */
    public void insertAfterSuper(String body, CtClass targetClass, String targetMethodName, CtClass... targetMethodParams)
            throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableMethod insertableMethod = new InsertableMethodBuilder(mSignatureExtractor)
                .insertIntoClass(targetClass)
                .afterSuper(targetMethodName, targetMethodParams)
                .withBody(body)
                .createInsertableMethod();
        addOrInsertMethod(insertableMethod);
    }


    /**
     * Inserts java instructions into all constructors a given class.
     *
     * @param insertableConstructor contains all information about insertion.
     * @throws CannotCompileException         if the source contained in insertableMethod can't be compiled.
     * @throws AfterBurnerImpossibleException if something else goes wrong, wraps other exceptions.
     */
    public void insertConstructor(InsertableConstructor insertableConstructor)
            throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        List<CtConstructor> constructorList = extractExistingConstructors(insertableConstructor);
        log.info("constructor count: " + constructorList.size());

        if (constructorList.isEmpty()) {
            throw new AfterBurnerImpossibleException("No suitable constructor was found in class " + insertableConstructor.getClassToInsertInto().getName() + ". Add a constructor that is accepted by the InsertableConstructor. Don't use non static inner classes.");
        }

        for (CtConstructor constructor : constructorList) {
            String constructorBody = insertableConstructor.getConstructorBody(constructor.getParameterTypes());
            constructor.insertBeforeBody(constructorBody);
        }
    }

    public void insertAllConstructors(String body, CtClass targetClass)
            throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableConstructor insertableConstructor = new InsertableConstructorBuilder()
                .insertIntoClass(targetClass)
                .withBody(body)
                .createInsertableConstructor();
        insertConstructor(insertableConstructor);
    }

    public void insertConstructor(String body, CtClass targetClass, CtClass... targetConstructorParams)
            throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableConstructor insertableConstructor = new InsertableConstructorBuilder()
                .insertIntoClass(targetClass)
                .intoConstructor(targetConstructorParams)
                .withBody(body)
                .createInsertableConstructor();
        insertConstructor(insertableConstructor);
    }

    /**
     * Returns the method named {@code methodName} in {@code classToTransform}. Null if not found.
     * Due to limitations of javassist, in case of multiple overloads, one of them only is returned.
     * (https://github.com/jboss-javassist/javassist/issues/9)
     *
     * @param classToTransform the class that should contain a method methodName.
     * @param methodName       the name of the method to retrieve.
     * @return the method named {@code methodName} in {@code classToTransform}. Null if not found.
     */
    private CtMethod extractExistingMethod(final CtClass classToTransform, String methodName, CtClass... methodParams) {
        try {
            return classToTransform.getDeclaredMethod(methodName, methodParams);
        } catch (Exception e) {
            return null;
        }
    }

    private CtMethod extractExistingMethod(final CtClass classToTransform, String methodName) {
        try {
            return classToTransform.getDeclaredMethod(methodName);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkIfMethodIsInvoked(CtMethod withinMethod, String invokedMethod) throws CannotCompileException {
        return new DetectMethodCallEditor(withinMethod, invokedMethod).checkIfisCallingMethod();
    }

    private List<CtConstructor> extractExistingConstructors(final InsertableConstructor insertableConstructor) throws NotFoundException, AfterBurnerImpossibleException {
        List<CtConstructor> constructors = new ArrayList<>();

        CtConstructor[] declaredConstructors = insertableConstructor.getClassToInsertInto().getDeclaredConstructors();
        for (CtConstructor constructor : declaredConstructors) {
            CtClass[] paramClasses = constructor.getParameterTypes();
            if (insertableConstructor.acceptParameters(paramClasses)) {
                constructors.add(constructor);
            }
        }

        return constructors;
    }
}
