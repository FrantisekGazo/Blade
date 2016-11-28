package eu.f3rog.blade.weaving.interfaces;

import java.util.List;

import eu.f3rog.javassist.JavassistHelper;
import eu.f3rog.javassist.exception.AfterBurnerImpossibleException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;


/**
 * Weaves MVP interface to View
 *
 * @author FrantisekGazo
 */
final class WeavedMvpViewIW
        extends WeavedMvpUiIW {

    @Override
    protected void weave(CtClass targetClass, JavassistHelper javassistHelper, List<String> presenterFieldNames)
            throws CannotCompileException, NotFoundException, AfterBurnerImpossibleException {

        ClassPool classPool = targetClass.getClassPool();

        // TODO : show I cause crash if user did not set ID to View ?!

        // ~> onRestoreInstanceState
        StringBuilder body = new StringBuilder();
        body.append("{");
        // FIXME : $1 is a Parcelable NOT a Bundle !!!
        body.append("this.setWeavedState($1);"); // initialize view STATE
        body.append("this.setWeavedId(").append(PM).append(".getId(this, this.getContext()));"); // initialize view ID
        body.append("}");
        javassistHelper.insertBeforeBody(body.toString(), targetClass, "onRestoreInstanceState", classPool.get("android.os.Parcelable"));

        // ~> onAttachedToWindow
        body.setLength(0);
        body.append("{");
        body.append("if (this.getWeavedId() == null) {")
                .append("this.setWeavedId(").append(PM).append(".getId(this, this.getContext()));") // initialize view ID
                .append("}");
        body.append("}");
        javassistHelper.insertBeforeBody(body.toString(), targetClass, "onAttachedToWindow");

        // ~> onSaveInstanceState
        body.setLength(0);
        body.append("{");
        body.append("this.setOnSaveCalled();");
        // FIXME : state is not a parameter !
        body.append(PM).append(".saveViewId($1, this.getWeavedId());"); // save view ID
        for (String presenterFieldName : presenterFieldNames) { // save each declared presenter
            body.append(PM).append(".save($1, \"").append(presenterFieldName).append("\", ").append(presenterFieldName).append(");");
        }
        body.append("}");
        javassistHelper.insertBeforeBody(body.toString(), targetClass, "onSaveInstanceState", classPool.get("android.os.Bundle"));

        // ~> onDetachedFromWindow
        body.setLength(0);
        body.append("{");
        for (String presenterFieldName : presenterFieldNames) { // unbind/remove each declared presenter
            body.append(PM).append(".onViewDestroy(this, \"").append(presenterFieldName).append("\", ").append(presenterFieldName).append(");");
        }
        body.append("}");
        javassistHelper.insertBeforeBody(body.toString(), targetClass, "onDetachedFromWindow");
    }
}
