package eu.f3rog.blade.weaving.util;

import eu.f3rog.afterburner.AfterBurner;

/**
 * Class {@link AWeaver} is used for bytecode weaving.
 *
 * @author FrantisekGazo
 * @version 2016-03-19
 */
public abstract class AWeaver implements IWeaver {

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

    protected boolean isDebug() {
        return mDebug;
    }

    protected AfterBurner getAfterBurner() {
        return mAfterBurner;
    }

    protected void log(String msg, Object... args) {
        if (mDebug) {
            String format = String.format("@ %s : %s\n", getClass().getSimpleName(), msg);
            System.out.printf(format, args);
        }
    }

}
