package eu.f3rog.blade.sample.mvp.model;

/**
 * Class {@link Data}
 *
 * @author FrantisekGazo
 * @version 2016-02-15
 */
public class Data {

    private int mWait;
    private String mText;

    public Data(int wait, String text) {
        mWait = wait;
        mText = text;
    }

    public int getWait() {
        return mWait;
    }

    public String getText() {
        return mText;
    }

    // data has to have custom toString() method so that tag can be created correctly
    @Override
    public String toString() {
        return mText;
    }

}
