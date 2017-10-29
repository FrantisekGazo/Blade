package eu.f3rog.ptu

/* package */ final class GradleTempFileBuilder
        extends TempFileBuilder {

    private final GradleConfig mConfig

    /* package */

    GradleTempFileBuilder(final GradleConfig config) {
        super('build.gradle')
        mConfig = config
    }

    @Override
    public String getBody() {
        final String androidConfig
        if (mConfig.hasAndroidPlugin()) {
            androidConfig = """android {
    compileSdkVersion 26
    buildToolsVersion "26.0.2"

    defaultConfig {
        applicationId "com.example"
        minSdkVersion 14
        targetSdkVersion 26
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    lintOptions {
        abortOnError false
    }
}
            """
        } else {
            androidConfig = ""
        }


        return """buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        ${mConfig.getRepository()}
    }
    dependencies {
        ${mConfig.getFormattedClasspaths()}
    }
}

${mConfig.getFormattedPlugins()}

${androidConfig}

dependencies {
    ${mConfig.getFormattedDependencies()}
}

project.plugins.each {
    print 'Plugin: '
    println it
}
        """
    }

    @Override
    public TempFileBuilder[] getRelatedFiles() {
        return mConfig.hasAndroidPlugin() ? [new AndroidManifestTempFileBuilder(), new MainActivityTempFileBuilder()] : []
    }
}
