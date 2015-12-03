package eu.f3rog.blade.weaving.util;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;
import javassist.build.JavassistBuildException;

/**
 * Class {@link AWeaver} is used for injecting classes with bytecode dependent on other application classes.
 *
 * @author FrantisekGazo
 * @version 2015-11-04
 */
public abstract class AWeaver implements IWeaver {

    private final Set<String> mWaitingForClasses;
    private final Set<String> mFoundClasses;
    private final Set<CtClass> mStack;
    private String mDestinationDir;
    private boolean mDebug;

    /**
     * Constructor
     *
     * @param requiredClasses List of application classes that are necessary for class transformation.
     */
    public AWeaver(Collection<String> requiredClasses, boolean debug) {
        this.mWaitingForClasses = new HashSet<>(requiredClasses);
        this.mFoundClasses = new HashSet<>();
        this.mStack = new HashSet<>();
        this.mDebug = debug;
    }

    @Override
    public final void setDestinationDirectory(File dst) {
        mDestinationDir = dst.toString();
    }

    @Override
    public final boolean shouldTransform(CtClass candidateClass) throws JavassistBuildException {
        // stop waiting for class if it is required
        if (mWaitingForClasses.remove(candidateClass.getName())) {
            mFoundClasses.add(candidateClass.getName());
        }
        // if all required classes were processed, then transform all stacked classes
        if (mWaitingForClasses.isEmpty() && !mStack.isEmpty()) {
            checkStackedClasses();
        }

        boolean shouldTransform = needTransformation(candidateClass);

        if (!shouldTransform) {
            return false;
        }

        if (!mWaitingForClasses.isEmpty()) {
            candidateClass.stopPruning(true);
            // if not all required classes were processed, than stack this for later
            mStack.add(candidateClass);
            return false;
        } else {
            // transform current class
            return true;
        }
    }

    private void checkStackedClasses() throws JavassistBuildException {
        // transform previously stacked classes
        for (CtClass ctClass : mStack) {
            if (ctClass.isFrozen()) ctClass.defrost();
            applyTransformations(ctClass);
            try {
                ctClass.writeFile(mDestinationDir);
            } catch (Exception e) {
                e.printStackTrace();
                throw new JavassistBuildException(e);
            }
        }
        mStack.clear();
    }

    public abstract boolean needTransformation(CtClass candidateClass) throws JavassistBuildException;

    protected void log(String msg, Object... args) {
        if (mDebug) {
            String format = String.format("@ %s : %s\n", getClass().getSimpleName(), msg);
            System.out.printf(format, args);
        }
    }

}
