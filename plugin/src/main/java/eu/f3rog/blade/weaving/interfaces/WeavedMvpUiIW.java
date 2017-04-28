package eu.f3rog.blade.weaving.interfaces;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.f3rog.javassist.JavassistHelper;
import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;


/**
 * Base class for weaving a MVP interface
 *
 * @author FrantisekGazo
 */
abstract class WeavedMvpUiIW
        extends InterfaceWeaver {

    protected static final String PM = "blade.mvp.PresenterManager.getInstance()";

    @Override
    public final void weave(CtClass interfaceClass, CtClass targetClass, JavassistHelper javassistHelper)
            throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException {

        ClassPool classPool = targetClass.getClassPool();

        boolean implementedBySuperclass = willBeImplementedBySuperclass(targetClass, interfaceClass);

        if (!implementedBySuperclass) {
            // add interface
            targetClass.addInterface(interfaceClass);

            // add ID field
            CtField field = new CtField(classPool.get("java.lang.String"), "mWeavedId", targetClass);
            field.setModifiers(Modifier.PRIVATE);
            targetClass.addField(field);
            // getter
            CtMethod getterMethod = new CtMethod(
                    classPool.get("java.lang.String"),
                    "getWeavedId",
                    null,
                    targetClass
            );
            getterMethod.setBody("{ return this.mWeavedId; }");
            targetClass.addMethod(getterMethod);
            // setter
            CtMethod setterMethod = new CtMethod(
                    classPool.get("void"),
                    "setWeavedId",
                    new CtClass[]{classPool.get("java.lang.String")},
                    targetClass
            );
            setterMethod.setBody("{ this.mWeavedId = $1; }");
            targetClass.addMethod(setterMethod);

            // add STATE field
            field = new CtField(classPool.get("android.os.Bundle"), "mWeavedState", targetClass);
            field.setModifiers(Modifier.PRIVATE);
            targetClass.addField(field);
            // getter
            getterMethod = new CtMethod(
                    classPool.get("android.os.Bundle"),
                    "getWeavedState",
                    null,
                    targetClass
            );
            getterMethod.setBody("{ return this.mWeavedState; }");
            targetClass.addMethod(getterMethod);
            // setter
            setterMethod = new CtMethod(
                    classPool.get("void"),
                    "setWeavedState",
                    new CtClass[]{classPool.get("android.os.Bundle")},
                    targetClass
            );
            setterMethod.setBody("{ this.mWeavedState = $1; }");
            targetClass.addMethod(setterMethod);

            // add SAVE CALL field
            field = new CtField(classPool.get("boolean"), "mWasOnSaveCalled", targetClass);
            field.setModifiers(Modifier.PRIVATE);
            targetClass.addField(field);
            // getter
            getterMethod = new CtMethod(
                    classPool.get("boolean"),
                    "wasOnSaveCalled",
                    null,
                    targetClass
            );
            getterMethod.setBody("{ return this.mWasOnSaveCalled; }");
            targetClass.addMethod(getterMethod);
            // setter
            setterMethod = new CtMethod(
                    classPool.get("void"),
                    "setOnSaveCalled",
                    null,
                    targetClass
            );
            setterMethod.setBody("{ this.mWasOnSaveCalled = true; }");
            targetClass.addMethod(setterMethod);
        }

        List<String> presenterFieldNames = getPresenterFieldNames(targetClass);

        // ~> _MembersInjector
        DaggerMiddleMan daggerMiddleMan = new DaggerMiddleMan();
        daggerMiddleMan.weaveFor(targetClass, presenterFieldNames, PM + ".get", javassistHelper);

        weave(targetClass, javassistHelper, presenterFieldNames);
    }

    protected abstract void weave(CtClass targetClass, JavassistHelper javassistHelper, List<String> presenterFieldNames)
            throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException;

    private List<String> getPresenterFieldNames(CtClass targetClass) throws NotFoundException {
        List<String> presenterFieldNames = new ArrayList<>();

        CtClass ctClass = targetClass;
        while (ctClass != null) {
            ClassPool classPool = ctClass.getClassPool();
            CtField[] declaredFields = ctClass.getDeclaredFields();
            CtClass presenterInterface = classPool.get("blade.mvp.IPresenter");

            for (CtField declaredField : declaredFields) {
                if (declaredField.hasAnnotation(Inject.class) && declaredField.getType().subtypeOf(presenterInterface)) {
                    presenterFieldNames.add(declaredField.getName());
                }
            }
            ctClass = ctClass.getSuperclass();
        }

        return presenterFieldNames;
    }
}
