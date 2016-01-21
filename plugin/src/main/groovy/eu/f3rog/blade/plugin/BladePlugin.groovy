package eu.f3rog.blade.plugin

import eu.f3rog.blade.plugin.util.AWeavingPlugin
import eu.f3rog.blade.weaving.BladeWeaver
import eu.f3rog.blade.weaving.util.IWeaver
import org.gradle.api.Project

/**
 * Class {@link BladePlugin} adds dependencies to gradle project and applies Bytecode Weaving.
 *
 * @author FrantisekGazo
 * @version 2015-11-09
 */
public class BladePlugin extends AWeavingPlugin {

    public static final String NAME = "blade"

    @Override
    public IWeaver[] getTransformers(Project project) {
        return [
                new BladeWeaver(project.blade.debug)
        ]
    }

    @Override
    protected void configure(Project project) {
        project.android {
            packagingOptions {
                exclude 'META-INF/services/javax.annotation.processing.Processor'
            }
        }
        project.dependencies {
            // library
            compile 'eu.f3rog.blade:core:1.2.1'
            apt 'eu.f3rog.blade:compiler:1.2.1'
        }
    }

    @Override
    protected Class getPluginExtension() {
        return BladeExtension.class
    }

    @Override
    protected String getExtension() {
        return NAME
    }

}
