package eu.f3rog.automat.plugin

import eu.f3rog.automat.plugin.util.AWeavingPlugin
import eu.f3rog.automat.weaving.Weaver
import eu.f3rog.automat.weaving.util.IWeaver
import org.gradle.api.Project

/**
 * Class {@link Plugin} adds dependencies to gradle project and applies Bytecode Weaving.
 *
 * @author FrantisekGazo
 * @version 2015-11-09
 */
public class Plugin extends AWeavingPlugin {

    public static final String NAME = "automat"

    @Override
    public IWeaver[] getTransformers(Project project) {
        return [
                new Weaver(/* add extension params if necessary */)
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
            compile 'eu.f3rog.automat:annotations:0.2.0'
            apt 'eu.f3rog.automat:compiler:0.2.0'
        }
    }

    @Override
    protected Class getPluginExtension() {
        return Extension.class
    }

    @Override
    protected String getExtension() {
        return NAME
    }

}
