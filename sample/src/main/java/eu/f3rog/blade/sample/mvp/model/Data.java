package eu.f3rog.blade.sample.mvp.model;


public class Data {

    private final int mWait;
    private final int mCount;
    private final long mId;
    private final String mText;

    public Data(long id, int count, int wait, String text) {
        mId = id;
        mCount = count;
        mWait = wait;
        mText = text;
    }

    public long getId() {
        return mId;
    }

    public int getWait() {
        return mWait;
    }

    public int getCount() {
        return mCount;
    }

    public String getText() {
        return mText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Data data = (Data) o;

        if (mWait != data.mWait) return false;
        if (mId != data.mId) return false;
        if (mCount != data.mCount) return false;
        return mText != null ? mText.equals(data.mText) : data.mText == null;

    }

    @Override
    public int hashCode() {
        int result = mWait;
        result = 31 * result + (int) (mId ^ (mId >>> 32));
        result = 31 * result + mCount;
        result = 31 * result + (mText != null ? mText.hashCode() : 0);
        return result;
    }
}
