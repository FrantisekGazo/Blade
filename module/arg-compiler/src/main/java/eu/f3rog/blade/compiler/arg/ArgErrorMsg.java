package eu.f3rog.blade.compiler.arg;

import blade.Arg;
import eu.f3rog.blade.compiler.ErrorMsg;

/**
 * Enum {@link ArgErrorMsg}
 *
 * @author FrantisekGazo
 */
public interface ArgErrorMsg extends ErrorMsg {

    String Invalid_class_with_Arg = "Only Fragment subclass can contain @" + Arg.class.getSimpleName() + ".";
}
