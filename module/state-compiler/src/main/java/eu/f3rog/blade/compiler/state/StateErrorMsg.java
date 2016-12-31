package eu.f3rog.blade.compiler.state;

import blade.State;
import eu.f3rog.blade.compiler.ErrorMsg;

/**
 * Enum {@link StateErrorMsg}
 *
 * @author FrantisekGazo
 */
public interface StateErrorMsg extends ErrorMsg {

    String View_cannot_implement_state_methods = "View subclass containing @" + State.class.getSimpleName()
            + " cannot implement 'onSaveInstanceState()' nor 'onRestoreInstanceState()' methods."
            + " These methods will be implemented by Blade library.";
}
