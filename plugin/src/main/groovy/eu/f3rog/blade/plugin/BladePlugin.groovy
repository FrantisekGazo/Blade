package eu.f3rog.blade.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.yaml.snakeyaml.Yaml

public final class BladePlugin
        implements Plugin<Project> {

    public static final class BladeConfig {

        // non-debug by default
        public boolean debug = false
        // include all modules by default
        public String[] modules = []

        @Override
        public String toString() {
            return String.format("%s[debug: %b, modules: %s]",
                    BladeConfig.class.getSimpleName(), this.debug, Arrays.toString(this.modules))
        }
    }

    public static String ERROR_GRADLE_TOOLS_1_5_0_REQUIRED = "Blade plugin only supports android gradle plugin 1.5.0 or later!"
    public static String ERROR_ANDROID_PLUGIN_REQUIRED = "'com.android.application' or 'com.android.library' plugin required!"
    public static String ERROR_MODULE_DOES_NOT_EXIST = "Blade does not have module '%s'!"
    public static String ERROR_APT_IS_MISSING = "Apply apt plugin or update gradle plugin to >=2.2.0!"
    public static String ERROR_CONFIG_FILE_IS_MISSING = "Blade configuration file is missing! (more info here: https://github.com/FrantisekGazo/Blade/wiki#1-create-configuration-file)"

    public static String LIB_GROUP_ID = "eu.f3rog.blade"
    public static String LIB_VERSION = "2.6.2"
    public static String LIB_CONFIG_FILE_NAME = "blade"
    public static String[] LIB_MODULES = ["arg", "extra", "mvp", "parcel", "state"]

    private BladeConfig mConfig;

    @Override
    void apply(Project project) {
        // Make sure the project is either an Android application or library
        boolean isAndroidApp = project.plugins.withType(AppPlugin)
        boolean isAndroidLib = project.plugins.withType(LibraryPlugin)
        if (!isAndroidApp && !isAndroidLib) {
            throw new GradleException(ERROR_ANDROID_PLUGIN_REQUIRED)
        }

        // check gradle plugin
        if (!isTransformAvailable()) {
            throw new GradleException(ERROR_GRADLE_TOOLS_1_5_0_REQUIRED)
        }

        prepareConfig(project)

        project.repositories.add(project.getRepositories().jcenter())
        def apList = determineAnnotationProcessorPlugin(project)
        // core
        project.dependencies.add("compile", "$LIB_GROUP_ID:core:$LIB_VERSION")
        // modules
        for (final String moduleName : mConfig.modules) {
            project.dependencies.add("compile", "$LIB_GROUP_ID:$moduleName:$LIB_VERSION")
            for (final String ap : apList) {
                project.dependencies.add(ap, "$LIB_GROUP_ID:$moduleName-compiler:$LIB_VERSION")
            }
        }

        // apply bytecode weaving via Transform API
        project.android.registerTransform(new BladeTransformer(mConfig.debug))
    }

    private String[] determineAnnotationProcessorPlugin(Project project) {
        def usesAptPlugin = project.plugins.findPlugin('com.neenbedankt.android-apt') != null
        def isKotlinProject = project.plugins.findPlugin('kotlin-android') != null
        def hasAnnotationProcessorConfiguration = project.getConfigurations().findByName('annotationProcessor') != null
        // TODO : add a parameter in config if this should be specified by users ?!
        def preferAptOnKotlinProject = false

        if (usesAptPlugin) {
            return ['apt', 'androidTestApt']
        } else if (isKotlinProject && !preferAptOnKotlinProject) {
            return ['kapt']
        } else if (hasAnnotationProcessorConfiguration) {
            return ['annotationProcessor', 'androidTestAnnotationProcessor']
        } else {
            throw new IllegalStateException(ERROR_APT_IS_MISSING)
        }
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

        // load configuration file (if exists)
        final String filePath = project.projectDir.getAbsolutePath() + File.separator + LIB_CONFIG_FILE_NAME
        final File jsonConfigFile = new File(filePath + ".json")
        final File yamlConfigFile = new File(filePath + ".yaml")

        if (jsonConfigFile.exists()) {
            Map json = new JsonSlurper().parseText(jsonConfigFile.text)

            json.each { key, value ->
                switch (key) {
                    case "debug":
                        mConfig.debug = value
                        break
                    case "modules":
                        mConfig.modules = value
                        break
                    default:
                        throw new IllegalStateException("'$key' is not supported in ${LIB_CONFIG_FILE_NAME}.json!")
                }
            }
        } else if (yamlConfigFile.exists()) {
            Yaml yaml = new Yaml()
            mConfig = yaml.loadAs(new FileInputStream(yamlConfigFile), BladeConfig.class)
        } else {
            throw new IllegalStateException(ERROR_CONFIG_FILE_IS_MISSING)
        }

        // check module names
        mConfig.modules = checkModuleNames(mConfig.modules)

        System.out.println("used Blade config: " + mConfig)
    }

    private String[] checkModuleNames(final String[] values) {
        return values.collect {
            final String moduleName = it.toLowerCase() // ignore case
            if (!LIB_MODULES.contains(moduleName)) { // check if module exists
                throw new IllegalStateException(String.format(ERROR_MODULE_DOES_NOT_EXIST, it))
            }
            return moduleName
        }
    }
}