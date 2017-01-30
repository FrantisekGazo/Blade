package eu.f3rog.blade.sample.mvp.model;


import android.support.annotation.NonNull;

public final class ActorDetail {

    @NonNull
    private final String mBirthName;
    @NonNull
    private final String mBirthDate;
    @NonNull
    private final String mBio;

    public ActorDetail(@NonNull final String birthName,
                       @NonNull final String birthDate,
                       @NonNull final String bio) {
        mBirthName = birthName;
        mBirthDate = birthDate;
        mBio = bio;
    }

    @NonNull
    public String getBirthName() {
        return mBirthName;
    }

    @NonNull
    public String getBirthDate() {
        return mBirthDate;
    }

    @NonNull
    public String getBio() {
        return mBio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActorDetail that = (ActorDetail) o;

        if (!mBirthName.equals(that.mBirthName)) return false;
        if (!mBirthDate.equals(that.mBirthDate)) return false;
        return mBio.equals(that.mBio);

    }

    @Override
    public int hashCode() {
        int result = mBirthName.hashCode();
        result = 31 * result + mBirthDate.hashCode();
        result = 31 * result + mBio.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Birth Name: " + mBirthName +
                "\n\nBirth Date: " + mBirthDate +
                "\n\nBio: " + mBio;
    }
}
