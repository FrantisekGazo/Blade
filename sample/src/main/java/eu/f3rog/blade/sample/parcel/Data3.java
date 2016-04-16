package eu.f3rog.blade.sample.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Map;

/**
 * Class {@link Data3}
 *
 * @author FrantisekGazo
 * @version 2016-04-07
 */
@blade.Parcel
public class Data3 implements Parcelable {

    GenericData<String> g;
    List<String> l;
    Map<Integer, String> m;

    public Data3() {
    }

    public Data3(Parcel parcel) {
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
        return "Data3[" + g + ", " + l + ", " + m + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Data3 data3 = (Data3) o;

        if (g != null ? !g.equals(data3.g) : data3.g != null) return false;
        if (l != null ? !l.equals(data3.l) : data3.l != null) return false;
        return m != null ? m.equals(data3.m) : data3.m == null;

    }

    @Override
    public int hashCode() {
        int result = g != null ? g.hashCode() : 0;
        result = 31 * result + (l != null ? l.hashCode() : 0);
        result = 31 * result + (m != null ? m.hashCode() : 0);
        return result;
    }
}
