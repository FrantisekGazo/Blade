package eu.f3rog.blade.plugin

import eu.f3rog.blade.plugin.util.AWeavingPlugin
import eu.f3rog.blade.weaving.BladeWeaver
import eu.f3rog.blade.weaving.util.IWeaver
import groovy.json.JsonSlurper
import org.gradle.api.Project

/**
 * Class {@link BladePlugin} adds dependencies to gradle project and applies Bytecode Weaving.
 *
 * @author FrantisekGazo
 * @version 2015-11-09
 */
public class BladePlugin extends AWeavingPlugin {

    private static class BladeConfig {
        boolean debug = false;
        String[] modules = ["arg", "extra", "state", "mvp"];
    }

    public static final String PLUGIN_NAME = "blade"
    public static final String PACKAGE_NAME = "eu.f3rog.blade"
    public static final String CONFIG_FILE_NAME = "blade.json"
    public static final String VERSION = "2.0.0"

    private BladeConfig mConfig;

    @Override
    public IWeaver[] getTransformers(Project project) {
        return [
                new BladeWeaver(mConfig.debug)
        ]
    }

    @Override
    protected void configure(Project project) {
        parseConfiguration(project)

        project.android {
            packagingOptions {
                exclude 'META-INF/services/javax.annotation.processing.Processor'
            }
        }
        project.dependencies {
            // library
            compile "$PACKAGE_NAME:core:$VERSION"

            for (String moduleName : mConfig.modules) {
                compile "$PACKAGE_NAME:module-$moduleName:$VERSION"
                apt "$PACKAGE_NAME:module-$moduleName-compiler:$VERSION"
            }
        }
    }

    @Override
    protected Class getPluginExtension() {
        return BladeExtension.class
    }

    @Override
    protected String getExtension() {
        return PLUGIN_NAME
    }

    private void parseConfiguration(Project project) {
        mConfig = new BladeConfig();

        File configFile = new File(project.projectDir.getAbsolutePath() + File.separator + CONFIG_FILE_NAME)

        if (!configFile.exists()) {
            return;
        }

        Map json = new JsonSlurper().parseText(configFile.text)

        json.each { key, value ->
            switch (key) {
                case "debug":
                    mConfig.debug = value
                    break;
                case "modules":
                    mConfig.modules = value
                    break;
                default:
                    throw new IllegalStateException("'$key' is not supported in $CONFIG_FILE_NAME.")
            }
        }

    }

}
