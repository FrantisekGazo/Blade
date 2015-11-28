package eu.f3rog.automat.weaving;

import com.github.stephanenicolas.afterburner.AfterBurner;

import java.util.Arrays;
import java.util.List;

import eu.f3rog.automat.weaving.util.AWeaver;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.build.JavassistBuildException;

import static eu.f3rog.automat.weaving.util.WeavingUtil.isSubclassOf;

public class Weaver extends AWeaver {

    private interface Class {
        // android classes
        String FRAGMENT = "android.app.Fragment";
        String SUPPORT_FRAGMENT = "android.support.v4.app.Fragment";
        // android classes
        String ACTIVITY = "android.app.Activity";
        String APP_COMPAT_ACTIVITY = "android.support.v7.app.AppCompatActivity";
        // generated classes
        String INJECTOR = "automat.Injector";
    }

    private interface Method {
        // android classes
        String ON_CREATE = "onCreate";
        String ON_ATTACH = "onAttach";
        // generated classes
        String INJECT = "inject";
    }

    private static final List<String> REQUIRED_CLASSES = Arrays.asList(
            Class.INJECTOR
    );

    private AfterBurner mAfterBurner;

    /**
     * Constructor
     */
    public Weaver() {
        super(REQUIRED_CLASSES);
        mAfterBurner = new AfterBurner();
    }

    @Override
    public boolean needTransformation(CtClass candidateClass) throws JavassistBuildException {
        try {
            log("needTransformation ? %s", candidateClass.getName());
            return isSubclassOf(candidateClass,
                    Class.ACTIVITY,
                    Class.APP_COMPAT_ACTIVITY,
                    Class.FRAGMENT,
                    Class.SUPPORT_FRAGMENT);
        } catch (Exception e) {
            log("needTransformation - failed");
            e.printStackTrace();
            throw new JavassistBuildException(e);
        }
    }

    @Override
    public void applyTransformations(CtClass classToTransform) throws JavassistBuildException {
        log("applyTransformations - %s", classToTransform.getName());
        try {
            // ACTIVITY
            if (isSubclassOf(classToTransform, Class.ACTIVITY, Class.APP_COMPAT_ACTIVITY)) {
                weaveActivity(classToTransform);
            }
            // FRAGMENT
            else if (isSubclassOf(classToTransform, Class.FRAGMENT, Class.SUPPORT_FRAGMENT)) {
                weaveFragment(classToTransform);
            }
            // nothing done
            else {
                log("applyTransformations - NOTHING done");
            }
            log("applyTransformations - done");
        } catch (Exception e) {
            log("applyTransformations - failed");
            e.printStackTrace();
            throw new JavassistBuildException(e);
        }
    }

    private boolean isSupported(CtClass classToTransform) {
        try {
            classToTransform.getClassPool().get(Class.INJECTOR).getDeclaredMethod(Method.INJECT, new CtClass[]{classToTransform});
            return true;
        } catch (NotFoundException e) {
            //log("No inject() method");
            return false;
        }
    }

    private void weaveActivity(CtClass classToTransform) throws Exception {
        if (!isSupported(classToTransform)) return;

        String body = String.format("{ %s.%s(this); }", Class.INJECTOR, Method.INJECT);
        // weave into method
        mAfterBurner.beforeOverrideMethod(classToTransform, Method.ON_CREATE, body);
        log("Weaved: %s", body);
    }

    private void weaveFragment(CtClass classToTransform) throws Exception {
        if (!isSupported(classToTransform)) return;

        String body = String.format("{ %s.%s(this); }", Class.INJECTOR, Method.INJECT);
        // weave into method
        mAfterBurner.beforeOverrideMethod(classToTransform, Method.ON_ATTACH, body);
        log("Weaved: %s", body);
    }

}
