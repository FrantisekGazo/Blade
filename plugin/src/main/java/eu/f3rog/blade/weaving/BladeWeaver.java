package eu.f3rog.blade.weaving;

import java.util.ArrayList;
import java.util.List;

import eu.f3rog.blade.weaving.util.AWeaver;
import eu.f3rog.blade.weaving.util.WeavingUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.build.JavassistBuildException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;

import static eu.f3rog.blade.weaving.util.WeavingUtil.getAnnotations;
import static eu.f3rog.blade.weaving.util.WeavingUtil.implementsInterface;

public class BladeWeaver extends AWeaver {

    private interface Class {
        // android classes
        String FRAGMENT = "android.app.Fragment";
        String SUPPORT_FRAGMENT = "android.support.v4.app.Fragment";
        // android classes
        String ACTIVITY = "android.app.Activity";
        String APP_COMPAT_ACTIVITY = "android.support.v7.app.AppCompatActivity";
        // generated classes
        String MIDDLE_MAN = "blade.MiddleMan";
        String WEAVE_GUIDE = "blade.WeaverGuide";
        String WEAVE = "blade.Weave";
    }

    private interface Method {
        // android classes
        String ON_CREATE = "onCreate";
        String ON_ATTACH = "onAttach";
        // generated classes
        String INJECT = "inject";
    }

    private List<String> mClassesToTransform = null;

    /**
     * Constructor
     */
    public BladeWeaver(boolean debug) {
        super(debug);
    }

    private void loadClassesToTransform(ClassPool classPool) {
        mClassesToTransform = new ArrayList<>();
        CtClass cc;
        try {
            cc = classPool.get(Class.WEAVE_GUIDE);
        } catch (NotFoundException e) {
            log("NOT FOUND!");
            return;
        }

        AnnotationsAttribute attr = getAnnotations(cc);
        if (attr == null) {
            log("NULL!");
            return;
        }

        for (Annotation annotation : attr.getAnnotations()) {
            if (!annotation.getTypeName().equals(Class.WEAVE)) {
                continue;
            }
            ArrayMemberValue value = (ArrayMemberValue) annotation.getMemberValue("value");
            MemberValue[] values = value.getValue();
            for (MemberValue memberValue : values) {
                String className = memberValue.toString().replaceAll("\"", "");
                log("Require transformation: >%s<", className);
                mClassesToTransform.add(className);
            }
        }
    }

    @Override
    public boolean shouldTransform(CtClass candidateClass) throws JavassistBuildException {
        if (mClassesToTransform == null) {
            loadClassesToTransform(candidateClass.getClassPool());
        }

        try {
            //log("needTransformation ? %s", candidateClass.getName());
            return mClassesToTransform.contains(candidateClass.getName());
        } catch (Exception e) {
            log("needTransformation failed on class %s", candidateClass.getName());
            e.printStackTrace();
            throw new JavassistBuildException(e);
        }
    }

    @Override
    public void applyTransformations(CtClass classToTransform) throws JavassistBuildException {
        log("Applying transformation to %s", classToTransform.getName());
        try {
            // ACTIVITY
            if (WeavingUtil.isSubclassOf(classToTransform, Class.ACTIVITY, Class.APP_COMPAT_ACTIVITY)) {
                weaveActivity(classToTransform);
            }
            // FRAGMENT
            else if (WeavingUtil.isSubclassOf(classToTransform, Class.FRAGMENT, Class.SUPPORT_FRAGMENT)) {
                weaveFragment(classToTransform);
            }
            // nothing done
            else {
                log("Nothing changed");
            }
            log("Transformation done");
        } catch (Exception e) {
            log("Transformation failed!");
            e.printStackTrace();
            throw new JavassistBuildException(e);
        }
    }

    private boolean isSupported(CtClass classToTransform) {
        try {
            classToTransform.getClassPool().get(Class.MIDDLE_MAN).getDeclaredMethod(Method.INJECT, new CtClass[]{classToTransform});
            return true;
        } catch (NotFoundException e) {
            //log("No inject() method");
            return false;
        }
    }

    private void weaveActivity(CtClass classToTransform) throws Exception {
        if (!isSupported(classToTransform)) return;

        String body = String.format("{ %s.%s(this); }", Class.MIDDLE_MAN, Method.INJECT);
        // weave into method
        getAfterBurner().beforeOverrideMethod(classToTransform, Method.ON_CREATE, body);
        log("%s weaved into %s", body, Method.ON_CREATE);
    }

    private void weaveFragment(CtClass classToTransform) throws Exception {
        if (!isSupported(classToTransform)) return;

        String body = String.format("{ %s.%s(this); }", Class.MIDDLE_MAN, Method.INJECT);
        // weave into method
        getAfterBurner().beforeOverrideMethod(classToTransform, Method.ON_ATTACH, body);
        log("%s weaved into %s", body, Method.ON_ATTACH);
    }

}
