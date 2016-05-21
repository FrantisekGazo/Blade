package eu.f3rog.blade.sample.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Class {@link Data2}
 *
 * @author FrantisekGazo
 * @version 2016-03-27
 */
@blade.Parcel
public class Data2 implements Parcelable {

    boolean flag;
    int number;
    String text;
    double[] array;
    Data d;

    public Data2() {
    }

    public Data2(Parcel parcel) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("Data2[");

        sb.append("d = ").append(d);
        sb.append(", ");
        sb.append("number = ").append(number);
        sb.append(", ");
        sb.append("flag = ").append(flag);
        sb.append(", ");
        sb.append("text = '").append(text).append("'");
        sb.append(", ");
        sb.append("array = {");
        if (array == null) {
            sb.append("null");
        } else {
            for (int i = 0; i < array.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(array[i]);
            }
        }
        sb.append("}");


        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Data2 data2 = (Data2) o;

        if (flag != data2.flag) return false;
        if (number != data2.number) return false;
        if (text != null ? !text.equals(data2.text) : data2.text != null) return false;
        if (!Arrays.equals(array, data2.array)) return false;
        return d != null ? d.equals(data2.d) : data2.d == null;
    }

    @Override
    public int hashCode() {
        int result = (flag ? 1 : 0);
        result = 31 * result + number;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(array);
        result = 31 * result + (d != null ? d.hashCode() : 0);
        return result;
    }
}
