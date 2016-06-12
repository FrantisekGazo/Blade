package eu.f3rog.blade.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.neenbedankt.gradle.androidapt.AndroidAptPlugin
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class BladePlugin implements Plugin<Project> {

    private static class BladeConfig {

        // non-debug by default
        boolean debug = false
        // include all modules by default
        String[] modules = LIB_MODULES
    }

    static String GRADLE_TOOLS_1_5_0_REQUIRED = "Blade plugin only supports android gradle plugin 1.5.0 or later."
    static String ANDROID_PLUGIN_REQUIRED = "'com.android.application' or 'com.android.library' plugin required."

    static String LIB_GROUP_ID = "eu.f3rog.blade"
    static String LIB_VERSION = "2.2.1-beta1"
    static String LIB_CONFIG_FILE_NAME = "blade.json"
    static String[] LIB_MODULES = ["arg", "extra", "mvp", "parcel", "state"]

    private BladeConfig mConfig;

    @Override
    void apply(Project project) {
        // Make sure the project is either an Android application or library
        boolean isAndroidApp = project.plugins.withType(AppPlugin)
        boolean isAndroidLib = project.plugins.withType(LibraryPlugin)
        if (!isAndroidApp && !isAndroidLib) {
            throw new GradleException(ANDROID_PLUGIN_REQUIRED)
        }

        // check gradle plugin
        if (!isTransformAvailable()) {
            throw new GradleException(GRADLE_TOOLS_1_5_0_REQUIRED)
        }

        prepareConfig(project)

        boolean isKotlinProject = project.plugins.find {
            it.getClass().name == "org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper"
        }

        if (!isKotlinProject) {
            project.plugins.apply(AndroidAptPlugin)

            project.android {
                packagingOptions {
                    exclude 'META-INF/services/javax.annotation.processing.Processor'
                }
            }
        }

        project.repositories.add(project.getRepositories().jcenter())

        // add dependencies
        String apt = isKotlinProject ? "kapt" : "apt"
        // core
        project.dependencies.add("compile", "$LIB_GROUP_ID:core:$LIB_VERSION")
        // modules
        for (String moduleName : mConfig.modules) {
            project.dependencies.add("compile", "$LIB_GROUP_ID:$moduleName:$LIB_VERSION")
            project.dependencies.add(apt, "$LIB_GROUP_ID:$moduleName-compiler:$LIB_VERSION")
        }

        // apply bytecode weaving via Transform API
        project.android.registerTransform(new BladeTransformer(mConfig.debug))
    }

    private static boolean isTransformAvailable() {
        try {
            Class.forName('com.android.build.api.transform.Transform')
            return true
        } catch (Exception ignored) {
            return false
        }
    }

    private void prepareConfig(Project project) {
        mConfig = new BladeConfig()

        File configFile = new File(project.projectDir.getAbsolutePath() + File.separator + LIB_CONFIG_FILE_NAME)

        if (!configFile.exists()) {
            return
        }

        Map json = new JsonSlurper().parseText(configFile.text)

        json.each { key, value ->
            switch (key) {
                case "debug":
                    mConfig.debug = value
                    break
                case "modules":
                    mConfig.modules = value.each {
                        String moduleName = it.toLowerCase() // ignore case
                        if (!LIB_MODULES.contains(moduleName)) { // check if module exists
                            throw new IllegalStateException("Blade does not have module '$it'.")
                        }
                        return moduleName
                    }
                    break
                default:
                    throw new IllegalStateException("'$key' is not supported in $LIB_CONFIG_FILE_NAME.")
            }
        }

    }

    private static ConfigObject parseLibConfig() {
        FileInputStream fis = new FileInputStream("../gradle.properties")
        Properties prop = new Properties()
        prop.load(fis)
        ConfigObject config = new ConfigSlurper().parse(prop)
        fis.close()
        return config
    }
}