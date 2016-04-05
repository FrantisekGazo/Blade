package eu.f3rog.blade.sample.parcel;

import android.os.Parcel;

import java.util.Arrays;

/**
 * Class {@link SubData2}
 *
 * @author FrantisekGazo
 * @version 2016-03-27
 */
@blade.Parcel
public class SubData2 extends Data2 {

    String[] stringArray;

    public SubData2() {
    }

    public SubData2(Parcel parcel) {
        super(parcel);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SubData2[");

        sb.append("inherited = ").append(super.toString());
        sb.append(", ");
        sb.append("stringArray = {");
        if (stringArray == null) {
            sb.append("null");
        } else {
            for (int i = 0; i < stringArray.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(stringArray[i]);
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
        if (!super.equals(o)) return false;

        SubData2 subData2 = (SubData2) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(stringArray, subData2.stringArray);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(stringArray);
        return result;
    }
}
