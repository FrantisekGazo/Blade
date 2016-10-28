package eu.f3rog.blade.weaving;

import java.util.HashMap;
import java.util.Map;

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
 * Class {@link InterfaceWeaver}
 *
 * @author FrantisekGazo
 * @version 2016-02-22
 */
public class Interfaces {

    private static Map<String, InterfaceWeaver> sSupportedInterfaces = null;

    public static void weaveInterface(CtClass interfaceClass, CtClass targetClass, JavassistHelper javassistHelper) throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException {
        if (sSupportedInterfaces == null) {
            initSupportedInterfaces();
        }

        InterfaceWeaver weaver = sSupportedInterfaces.get(interfaceClass.getName());
        if (weaver == null) {
            throw new IllegalArgumentException("Interface not supported");
        }
        weaver.weave(interfaceClass, targetClass, javassistHelper);
    }

    private static void initSupportedInterfaces() {
        sSupportedInterfaces = new HashMap<>();

        sSupportedInterfaces.put("eu.f3rog.blade.mvp.MvpActivity", new MvpActivityIW());
    }

    private interface InterfaceWeaver {
        void weave(CtClass interfaceClass, CtClass targetClass, JavassistHelper javassistHelper) throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException;
    }

    private static final class MvpActivityIW implements InterfaceWeaver {

        @Override
        public void weave(CtClass interfaceClass, CtClass targetClass, JavassistHelper javassistHelper) throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException {
            ClassPool classPool = targetClass.getClassPool();

            // add interface
            targetClass.addInterface(interfaceClass);
            // add field
            CtField field = new CtField(classPool.get("java.lang.String"), "mActivityId", targetClass);
            field.setModifiers(Modifier.PRIVATE);
            targetClass.addField(field);
            // add getter
            CtMethod getIdMethod = new CtMethod(
                    classPool.get("java.lang.String"),
                    "getId",
                    null,
                    targetClass
            );
            getIdMethod.setBody("return this.mActivityId;");
            // add to onCreate
            StringBuilder body = new StringBuilder();
            body.append("{")
                    .append("this.mActivityId = blade.mvp.PresenterManager.getActivityId($1);")
                    .append("blade.mvp.PresenterManager.restorePresentersFor(this, $1);");
            body.append("}");
            javassistHelper.insertBeforeBody(body.toString(), targetClass, "onCreate", classPool.get("android.os.Bundle"));
            // add to onSaveInstanceState
            body.setLength(0);
            body.append("{")
                    .append("blade.mvp.PresenterManager.putActivityId($1, this.mActivityId);")
                    .append("blade.mvp.PresenterManager.savePresentersFor(this, $1);");
            body.append("}");
            javassistHelper.insertBeforeBody(body.toString(), targetClass, "onSaveInstanceState", classPool.get("android.os.Bundle"));
            // add to onDestroy
            body.setLength(0);
            body.append("{")
                    .append("blade.mvp.PresenterManager.removePresentersFor(this);");
            body.append("}");
            javassistHelper.insertBeforeBody(body.toString(), targetClass, "onDestroy");

            targetClass.addMethod(getIdMethod);
        }

    }

}
