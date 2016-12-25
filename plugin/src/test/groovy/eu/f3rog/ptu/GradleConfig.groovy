package eu.f3rog.ptu


public final class GradleConfig {

    private List<String> mClasspaths = []
    private List<String> mPlugins = []
    private List<String> mDependencies = []

    public GradleConfig() {
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

    public List<String> getClasspaths() {
        return mClasspaths
    }

    public List<String> getPlugins() {
        return mPlugins
    }

    public List<String> getDependencies() {
        return mDependencies
    }
}
