package eu.f3rog.blade.sample.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class {@link IgnoreData}
 *
 * @author FrantisekGazo
 * @version 2016-04-05
 */
@blade.Parcel
public class IgnoreData implements Parcelable {

    Integer i;
    Double d;
    @blade.ParcelIgnore
    Float f;

    public IgnoreData() {
    }

    public IgnoreData(Parcel parcel) {
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
        return "IgnoreData[" + i + ", " + d + ", " + f + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IgnoreData that = (IgnoreData) o;

        if (i != null ? !i.equals(that.i) : that.i != null) return false;
        if (f != null ? !f.equals(that.f) : that.f != null) return false;
        return d != null ? d.equals(that.d) : that.d == null;

    }

    @Override
    public int hashCode() {
        int result = i != null ? i.hashCode() : 0;
        result = 31 * result + (f != null ? f.hashCode() : 0);
        result = 31 * result + (d != null ? d.hashCode() : 0);
        return result;
    }
}
