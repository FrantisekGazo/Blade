package eu.f3rog.blade.compiler.extra;

import blade.Extra;

/**
 * Enum {@link ExtraErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public interface ExtraErrorMsg extends eu.f3rog.blade.compiler.ErrorMsg {

    String Invalid_class_with_Extra = "Only Activity or Service subclass can contain @" + Extra.class.getSimpleName() + ".";

}
