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

        boolean hasOnRestore, hasOnSave;
        try {
            targetClass.getDeclaredMethod("onRestoreInstanceState", new CtClass[]{classPool.get("android.os.Parcelable")});
            hasOnRestore = true;
        } catch (NotFoundException e) {
            hasOnRestore = false;
        }
        try {
            targetClass.getDeclaredMethod("onSaveInstanceState", new CtClass[0]);
            hasOnSave = true;
        } catch (NotFoundException e) {
            hasOnSave = false;
        }

        // ~> onRestoreInstanceState
        StringBuilder body = new StringBuilder();
        body.append("{");
        body.append("android.os.Bundle b = (android.os.Bundle) $1;"); // initialize view STATE
        body.append("this.setWeavedState(b);"); // initialize view STATE
        body.append("this.setWeavedId(").append(PM).append(".getId(this, this.getContext()));"); // initialize view ID
        if (hasOnRestore) {
            javassistHelper.renameMethod(targetClass, "onRestoreInstanceState", "onRestoreInstanceState_BladeMvp", classPool.get("android.os.Parcelable"));
            body.append("this.onRestoreInstanceState_BladeMvp(b.getParcelable(\"USER_STATE\"));");
        } else {
            body.append("super.onRestoreInstanceState(b.getParcelable(\"PARENT_STATE\"));");
        }
        body.append("return;");
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
        body.append("android.os.Bundle b = new android.os.Bundle();");
        body.append(PM).append(".saveViewId(b, this.getWeavedId());"); // save view ID
        for (String presenterFieldName : presenterFieldNames) { // save each declared presenter
            body.append(PM).append(".save(b, \"").append(presenterFieldName).append("\", ").append(presenterFieldName).append(");");
        }
        if (hasOnSave) {
            javassistHelper.renameMethod(targetClass, "onSaveInstanceState", "onSaveInstanceState_BladeMvp");
            body.append("b.putParcelable(\"USER_STATE\", this.onSaveInstanceState_BladeMvp());");
        } else {
            body.append("b.putParcelable(\"PARENT_STATE\", super.onSaveInstanceState());");
        }
        body.append("return b;");
        body.append("}");
        javassistHelper.insertBeforeBody(body.toString(), targetClass, "onSaveInstanceState");

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
