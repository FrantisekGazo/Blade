package eu.f3rog.blade.weaving.interfaces;

import java.util.List;

import eu.f3rog.javassist.JavassistHelper;
import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;


/**
 * Weaves MVP interface to Activity
 *
 * @author FrantisekGazo
 */
final class WeavedMvpActivityIW
        extends WeavedMvpUiIW {

    @Override
    protected void weave(CtClass targetClass, JavassistHelper javassistHelper, List<String> presenterFieldNames)
            throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException {

        ClassPool classPool = targetClass.getClassPool();

        // ~> onCreate
        StringBuilder body = new StringBuilder();
        body.append("{")
                .append("this.setWeavedState($1);") // initialize view STATE
                .append("this.setWeavedId(").append(PM).append(".getId(this));") // initialize view ID
                .append("}");
        javassistHelper.insertBeforeBody(body.toString(), targetClass, "onCreate", classPool.get("android.os.Bundle"));

        // ~> onSaveInstanceState
        body.setLength(0);
        body.append("{");
        body.append("this.setOnSaveCalled();");
        body.append(PM).append(".saveViewId($1, this.getWeavedId());"); // save view ID
        for (String presenterFieldName : presenterFieldNames) { // save each declared presenter
            body.append(PM).append(".save($1, \"").append(presenterFieldName).append("\", ").append(presenterFieldName).append(");");
        }
        body.append("}");
        javassistHelper.insertBeforeBody(body.toString(), targetClass, "onSaveInstanceState", classPool.get("android.os.Bundle"));

        // ~> onDestroy
        body.setLength(0);
        body.append("{");
        for (String presenterFieldName : presenterFieldNames) { // unbind/remove each declared presenter
            body.append(PM).append(".onActivityDestroy(this, \"").append(presenterFieldName).append("\", ").append(presenterFieldName).append(");");
        }
        body.append("}");
        javassistHelper.insertBeforeBody(body.toString(), targetClass, "onDestroy");
    }
}
