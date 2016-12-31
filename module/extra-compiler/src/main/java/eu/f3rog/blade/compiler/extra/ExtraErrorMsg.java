package eu.f3rog.blade.compiler.extra;

import blade.Extra;

/**
 * Enum {@link ExtraErrorMsg}
 *
 * @author FrantisekGazo
 */
public interface ExtraErrorMsg extends eu.f3rog.blade.compiler.ErrorMsg {

    String Invalid_class_with_Extra = "Only Activity or Service subclass can contain @" + Extra.class.getSimpleName() + ".";
}
