package eu.f3rog.ptu


public final class GradleConfig {

    private String mVersionName
    private List<String> mClasspaths = []
    private List<String> mPlugins = []
    private List<String> mDependencies = []
    private List<String> mApDependencies = []

    public GradleConfig(String versionName) {
        mVersionName = versionName
    }

    public GradleConfig classpaths(final List<String> classpaths) {
        mClasspaths = classpaths
        return this
    }

    public GradleConfig plugins(final List<String> plugins) {
        mPlugins = plugins
        return this
    }

    public GradleConfig dependencies(final List<String> dependencies) {
        mDependencies = dependencies
        return this
    }

    public GradleConfig apDependencies(final List<String> dependencies) {
        mApDependencies = dependencies
        return this
    }

    public String getFormattedClasspaths() {
        return mClasspaths.collect({ "classpath '$it'" }).join("\n")
    }

    public String getFormattedPlugins() {
        return mPlugins.collect({ "apply plugin: '$it'" }).join("\n")
    }

    public String getRepository() {
        // google() does not work for the 1st test
        return isV3() ? '''
        maven {
            url 'https://maven.google.com/'
            name 'Google'
        }
        ''' : ''
    }

    public String getFormattedDependencies() {
        String apConfiguration = getUsedApConfiguration()

        List<String> deps = []
        deps.addAll(mDependencies.collect({ getConfiguration() + " '$it'" }))
        deps.addAll(mApDependencies.collect({ apConfiguration + " '$it'" }))
        return deps.join("\n")
    }

    public boolean hasAndroidPlugin() {
        return mPlugins.contains('com.android.application')
    }

    private boolean isV3() {
        return mVersionName.startsWith('3.')
    }

    private String getConfiguration() {
        return isV3() ? 'implementation' : 'compile'
    }

    private String getUsedApConfiguration() {
        for (String plugin : mPlugins) {
            if (plugin.contains('kotlin')) {
                return 'kapt'
            }
            if (plugin.contains('apt')) {
                return 'apt'
            }
        }
        return 'annotationProcessor'
    }
}
