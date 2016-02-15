package eu.f3rog.blade.weaving;

import eu.f3rog.afterburner.exception.AfterBurnerImpossibleException;
import eu.f3rog.afterburner.inserts.InsertableConstructor;
import eu.f3rog.blade.core.Weave;
import eu.f3rog.blade.weaving.util.AWeaver;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.build.JavassistBuildException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;

import static eu.f3rog.blade.weaving.util.WeavingUtil.getAnnotations;

public class BladeWeaver extends AWeaver {

    private static class Metadata {
        String into;
        CtClass[] args;
        String statement;
    }

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
            ClassPool classPool = classToTransform.getClassPool();
            CtClass helper = getHelper(classToTransform);

            // weave field metadata
            for (CtField field : helper.getDeclaredFields()) {
                Metadata metadata = loadWeaveMetadata(classPool, field);
                if (metadata != null) {
                    log("field named \"%s\"", field.getName());

                    CtField f = new CtField(field, classToTransform);
                    if (metadata.statement != null) {
                        log(" -> init with \"%s\"", metadata.statement);
                        classToTransform.addField(f, CtField.Initializer.byExpr(metadata.statement));
                    } else {
                        log(" -> no statement");
                        classToTransform.addField(f);
                    }
                }
            }

            // weave method metadata
            for (CtMethod method : helper.getDeclaredMethods()) {
                log("method named \"%s\"", method.getName());

                Metadata metadata = loadWeaveMetadata(classPool, method);
                if (metadata == null) { // nowhere
                    log(" -> nowhere");
                    continue;
                }

                String body = "{ " + metadata.statement + " }";

                if (metadata.into.length() == 0) { // weave into constructor
                    getAfterBurner().insertConstructor(new SpecificConstructor(body, classToTransform, metadata.args));
                    log(" -> %s weaved into constructor", body);
                } else { // weave into method
                    getAfterBurner().atBeginningOfOverrideMethod(body, classToTransform, metadata.into, metadata.args);
                    log(" -> %s weaved into %s", body, metadata.into);
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

    private Metadata loadWeaveMetadata(ClassPool classPool, CtMethod method) throws NotFoundException {
        AnnotationsAttribute attr = getAnnotations(method);
        if (attr == null) {
            return null;
        }

        Annotation a = attr.getAnnotation(Weave.class.getName());
        if (a == null) {
            return null;
        }

        Metadata metadata = new Metadata();
        // get INTO
        MemberValue val = a.getMemberValue("into");
        if (val != null) {
            metadata.into = val.toString().replaceAll("\"", "");
        }
        // get INTO ARGS
        metadata.args = loadClasses(a, "args", classPool);
        // get STATEMENT
        val = a.getMemberValue("statement");
        if (val != null) {
            metadata.statement = val.toString().replaceAll("\"", "");
            metadata.statement = metadata.statement.replaceAll("'", "\"");
        }

        return metadata;
    }

    private Metadata loadWeaveMetadata(ClassPool classPool, CtField field) throws NotFoundException {
        AnnotationsAttribute attr = getAnnotations(field);
        if (attr == null) {
            return null;
        }

        Annotation a = attr.getAnnotation(Weave.class.getName());
        if (a == null) {
            return null;
        }

        Metadata metadata = new Metadata();
        // get STATEMENT
        MemberValue val = a.getMemberValue("statement");
        if (val != null) {
            metadata.statement = val.toString().replaceAll("\"", "");
            metadata.statement = metadata.statement.replaceAll("'", "\"");
        }

        return metadata;
    }

    private CtClass[] loadClasses(Annotation a, String argName, ClassPool classPool) throws NotFoundException {
        ArrayMemberValue arrayMemberValue = (ArrayMemberValue) a.getMemberValue(argName);
        if (arrayMemberValue != null) {
            MemberValue[] values = arrayMemberValue.getValue();
            CtClass[] classes = new CtClass[values.length];
            for (int i = 0; i < values.length; i++) {
                String className = values[i].toString().replaceAll("\"", "");
                classes[i] = classPool.get(className);
            }
            return classes;
        } else {
            return new CtClass[0];
        }
    }

    private final class SpecificConstructor extends InsertableConstructor {

        private final CtClass[] mRequiredParams;
        private final String mBody;

        public SpecificConstructor(String body, CtClass classToInsertInto, CtClass... params) {
            super(classToInsertInto);
            mRequiredParams = params;
            mBody = body;
        }

        @Override
        public String getConstructorBody(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
            return mBody;
        }

        @Override
        public boolean acceptParameters(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
            if (paramClasses.length != mRequiredParams.length) {
                return false;
            }
            for (int i = 0, c = mRequiredParams.length; i < c; i++) {
                if (!mRequiredParams[i].equals(paramClasses[i])) {
                    return false;
                }
            }
            return true;
        }
    }

}
