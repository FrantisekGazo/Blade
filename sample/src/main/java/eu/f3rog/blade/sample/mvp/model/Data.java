package eu.f3rog.blade.sample.mvp.model;

/**
 * Class {@link Data}
 *
 * @author FrantisekGazo
 * @version 2016-02-15
 */
public class Data {

    private int mWait;
    private long mId;
    private String mText;

    public Data(long id, int wait, String text) {
        mId = id;
        mWait = wait;
        mText = text;
    }

    public long getId() {
        return mId;
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
        return String.valueOf(mId);
    }

}
