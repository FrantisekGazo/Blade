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

    public static final String ANDROID_PLUGIN_REQUIRED = "'com.android.application' or 'com.android.library' plugin required."

    public static final String LIB_CONFIG_FILE_NAME = "blade.json"
    public static final String LIB_PACKAGE_NAME = "eu.f3rog.blade"
    public static final String[] LIB_MODULES = ["arg", "extra", "mvp", "parcel", "state"]
    public static final String LIB_VERSION = "2.2.0-beta1"

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
            throw new GradleException("Blade plugin only supports android gradle plugin 1.5.0 or later.")
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
        project.dependencies.add("compile", "$LIB_PACKAGE_NAME:core:$LIB_VERSION")
        // modules
        for (String moduleName : mConfig.modules) {
            project.dependencies.add("compile", "$LIB_PACKAGE_NAME:$moduleName:$LIB_VERSION")
            project.dependencies.add(apt, "$LIB_PACKAGE_NAME:$moduleName-compiler:$LIB_VERSION")
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
                    mConfig.modules = value
                    break
                default:
                    throw new IllegalStateException("'$key' is not supported in $LIB_CONFIG_FILE_NAME.")
            }
        }

    }
}