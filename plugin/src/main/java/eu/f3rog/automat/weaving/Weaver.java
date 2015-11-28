package eu.f3rog.automat.weaving;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.InsertableMethodBuilder;
import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.afterburner.inserts.InsertableConstructor;

import java.util.Arrays;
import java.util.List;

import eu.f3rog.automat.Extra;
import eu.f3rog.automat.weaving.util.AWeaver;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
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
        String SET_CONTENT = "setContentView";
        String ON_ATTACH = "onAttach";
        // generated classes
        String INJECT = "inject";
    }

    private interface Field {
        String SUPPORTED = "SUPPORTED";
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
            //hasExtraField(candidateClass, Extra.class) || hasExtraField(candidateClass, Arg.class);
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
            } else if (isSubclassOf(classToTransform, Class.FRAGMENT, Class.SUPPORT_FRAGMENT)) {
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

    private void weaveActivity(CtClass classToTransform) throws Exception {
        try {
            classToTransform.getClassPool().get(Class.INJECTOR).getDeclaredMethod(Method.INJECT, new CtClass[]{classToTransform});
        } catch (NotFoundException e) {
            log("No inject() method");
            return;
        }

        String body = String.format("{ %s.%s(this); }", Class.INJECTOR, Method.INJECT);
        // weave into method
        mAfterBurner.beforeOverrideMethod(classToTransform, Method.ON_CREATE, body);
        log("Weaved: %s", body);
    }

/*
        CtMethod onCreateMethod = mAfterBurner.extractExistingMethod(classToTransform, Method.ON_CREATE);

        if (onCreateMethod != null) {
            log("Has onCreate method already");
            boolean isCallingSetContentView = mAfterBurner.checkIfMethodIsInvoked(onCreateMethod, "setContentView");

            log("onCreate invokes setContentView: " + isCallingSetContentView);
            String insertionMethod;
            if (isCallingSetContentView) {
                insertionMethod = Method.SET_CONTENT;
            } else {
                insertionMethod = "super." + Method.ON_CREATE;
            }

            InsertableMethodBuilder builder = new InsertableMethodBuilder(mAfterBurner);

            builder.insertIntoClass(classToTransform)
                    .inMethodIfExists(Method.ON_CREATE)
                    .afterACallTo(insertionMethod)
                    .withBody(body)
                    .elseCreateMethodIfNotExists("") //not used, we are sure the method exists
                    .doIt();

            log("Weaved: %s", body);
        } else {
            log("Does not have onCreate method yet");
            classToTransform.addMethod(CtNewMethod.make(createOnCreateMethod(body), classToTransform));
            log("Inserted " + Method.ON_CREATE);
        }
    }

    private String createOnCreateMethod(String body) {
        return "public void onCreate(android.os.Bundle savedInstanceState) { \n"
                + "super.onCreate(savedInstanceState);\n"
                + body
                + "\n}";
    }
*/
    private void weaveFragment(CtClass classToTransform) {

    }

    private boolean hasExtraField(CtClass candidateClass, java.lang.Class cls) {
        for (CtField field : candidateClass.getDeclaredFields()) {
            if (field.hasAnnotation(Extra.class)) return true;
        }
        return false;
    }

    private static class MyInsertableConstructor extends InsertableConstructor {

        private String mBody;

        public MyInsertableConstructor(CtClass classToInsertInto, String body) {
            super(classToInsertInto);
            this.mBody = body;
        }

        @Override
        public String getConstructorBody(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
            return mBody;
        }

        @Override
        public boolean acceptParameters(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
            return true; // into all
        }
    }

}
