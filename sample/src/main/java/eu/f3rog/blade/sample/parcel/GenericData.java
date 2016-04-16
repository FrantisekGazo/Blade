package eu.f3rog.blade.sample.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class {@link GenericData}
 *
 * @author FrantisekGazo
 * @version 2016-04-07
 */
@blade.Parcel
public class GenericData<T> implements Parcelable {

    T data;

    public GenericData(T data) {
        this.data = data;
    }

    public GenericData(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GenericData> CREATOR = new Creator<GenericData>() {
        @Override
        public GenericData createFromParcel(Parcel in) {
            return new GenericData(in);
        }

        @Override
        public GenericData[] newArray(int size) {
            return new GenericData[size];
        }
    };

    @Override
    public String toString() {
        return "GenericData[" + data + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericData<?> that = (GenericData<?>) o;

        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}
