package eu.f3rog.blade.compiler.parcel;

import blade.Parcel;
import eu.f3rog.blade.compiler.ErrorMsg;

/**
 * Enum {@link ParcelErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public interface ParcelErrorMsg extends ErrorMsg {

    String Invalid_Parcel_class = "Class annotated with @" + Parcel.class.getSimpleName() + " has to implement Parcelable interface.";
    String Parcel_class_without_constructor = "Class annotated with @" + Parcel.class.getSimpleName()
            + " has to have public constructor with parameter of type 'android.os.Parcel'"
            + " with empty body or calling 'super(parcel)' for inheritance.";

}
