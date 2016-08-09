package eu.f3rog.blade.compiler.prefs;

import blade.Prefs;
import eu.f3rog.blade.compiler.ErrorMsg;

/**
 * Enum {@link PrefsErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2016-08-09
 */
public interface PrefsErrorMsg extends ErrorMsg {

    String Invalid_type_with_Prefs = "Only interface can be annotated with @" + Prefs.class.getSimpleName() + ".";
}
