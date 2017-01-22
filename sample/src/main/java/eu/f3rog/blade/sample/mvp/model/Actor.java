package eu.f3rog.blade.sample.mvp.model;


import android.support.annotation.NonNull;

public final class Actor {

    private final long mId;
    @NonNull
    private final String mName;

    public Actor(final long id,
                 @NonNull final String name) {
        mId = id;
        mName = name;
    }

    public long getId() {
        return mId;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Actor actor = (Actor) o;

        if (mId != actor.mId) return false;
        return mName.equals(actor.mName);

    }

    @Override
    public int hashCode() {
        int result = (int) (mId ^ (mId >>> 32));
        result = 31 * result + mName.hashCode();
        return result;
    }
}
