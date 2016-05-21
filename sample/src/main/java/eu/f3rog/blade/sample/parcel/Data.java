package eu.f3rog.blade.sample.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class {@link Data}
 *
 * @author FrantisekGazo
 * @version 2016-03-27
 */
@blade.Parcel
public class Data implements Parcelable {

    Float fnum;
    Integer inum;

    public Data() {
    }

    public Data(Parcel parcel) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public String toString() {
        return String.format("Data[fnum = %s, inum = %s]", fnum, inum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Data data = (Data) o;

        if (fnum != null ? !fnum.equals(data.fnum) : data.fnum != null) return false;
        return inum != null ? inum.equals(data.inum) : data.inum == null;

    }

    @Override
    public int hashCode() {
        int result = fnum != null ? fnum.hashCode() : 0;
        result = 31 * result + (inum != null ? inum.hashCode() : 0);
        return result;
    }
}
