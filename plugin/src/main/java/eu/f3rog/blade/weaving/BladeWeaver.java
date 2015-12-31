package eu.f3rog.blade.weaving;

import eu.f3rog.blade.core.WeaveInto;
import eu.f3rog.blade.weaving.util.AWeaver;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.build.JavassistBuildException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

import static eu.f3rog.blade.weaving.util.WeavingUtil.getAnnotations;

public class BladeWeaver extends AWeaver {

    private static final String HELPER_NAME_FORMAT = "%s.%s_Helper";

    /**
     * Constructor
     */
    public BladeWeaver(boolean debug) {
        super(debug);
    }

    @Override
    public boolean shouldTransform(CtClass candidateClass) throws JavassistBuildException {
        try {
            //log("needTransformation ? %s", candidateClass.getName());
            return hasHelper(candidateClass);
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
            CtClass helper = getHelper(classToTransform);

            for (CtMethod method : helper.getDeclaredMethods()) {
                log("method %s", method.getName());

                String where = loadWeaveInto(method);
                if (where == null) { // nowhere
                    log(" -> nowhere");
                    continue;
                } else if (where.isEmpty()) { // into constructor
                    log(" -> into constructor");
                    // TODO : weave
                    throw new IllegalStateException("Weaving into constructor is not implemented yet!");
                } else {
                    String body = String.format("{ %s.%s(this); }", helper.getName(), method.getName());
                    // weave into method
                    getAfterBurner().beforeOverrideMethod(classToTransform, where, body);
                    log(" -> %s weaved into %s", body, where);
                }
            }

            log("Transformation done");
        } catch (Exception e) {
            log("Transformation failed!");
            e.printStackTrace();
            throw new JavassistBuildException(e);
        }
    }

    private CtClass getHelper(CtClass cls) throws NotFoundException {
        return cls.getClassPool()
                .get(String.format(HELPER_NAME_FORMAT, cls.getPackageName(), cls.getSimpleName()));
    }

    private boolean hasHelper(CtClass cls) {
        try {
            return getHelper(cls) != null;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private String loadWeaveInto(CtMethod method) {
        AnnotationsAttribute attr = getAnnotations(method);
        if (attr == null) {
            return null;
        }

        Annotation a = attr.getAnnotation(WeaveInto.class.getName());
        if (a == null) {
            return null;
        }

        return a.getMemberValue("value").toString().replaceAll("\"", "");
    }

}
