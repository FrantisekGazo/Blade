package eu.f3rog.blade.plugin

import eu.f3rog.blade.weaving.BladeWeaver
import eu.f3rog.blade.weaving.util.IWeaver
import org.gradle.api.Project

public final class BladeTransformer
        extends BaseTransformer {

    public BladeTransformer(Project project, boolean debug) {
        super(project, debug)
    }

    @Override
    String getName() {
        return "Blade"
    }

    @Override
    IWeaver getWeaver(boolean debug) {
        return new BladeWeaver(debug)
    }
}