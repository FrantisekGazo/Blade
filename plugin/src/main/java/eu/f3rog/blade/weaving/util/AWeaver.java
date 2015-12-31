package eu.f3rog.blade.weaving.util;

import com.github.stephanenicolas.afterburner.AfterBurner;

import java.io.File;

/**
 * Class {@link AWeaver} is used for injecting classes with bytecode dependent on other application classes.
 *
 * @author FrantisekGazo
 * @version 2015-11-04
 */
public abstract class AWeaver implements IWeaver {

    private String mDestinationDir;
    private boolean mDebug;
    private AfterBurner mAfterBurner;

    /**
     * Constructor
     *
     * @param debug If <code>true</code>, than logs will be shown.
     */
    public AWeaver(boolean debug) {
        this.mDebug = debug;
        this.mAfterBurner = new AfterBurner();
    }

    public boolean isDebug() {
        return mDebug;
    }

    public AfterBurner getAfterBurner() {
        return mAfterBurner;
    }

    @Override
    public final void setDestinationDirectory(File dst) {
        mDestinationDir = dst.toString();
    }

    public String getDestinationDir() {
        return mDestinationDir;
    }

    protected void log(String msg, Object... args) {
        if (mDebug) {
            String format = String.format("@ %s : %s\n", getClass().getSimpleName(), msg);
            System.out.printf(format, args);
        }
    }

}
