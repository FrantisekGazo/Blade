package eu.f3rog.blade.compiler.module.state;

import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.name.GPN;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link StateManagerBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class StateManagerBuilder
        extends BaseClassBuilder {

    public StateManagerBuilder() throws ProcessorError {
        super(GCN.STATE_MANAGER, GPN.BLADE);
    }
}
