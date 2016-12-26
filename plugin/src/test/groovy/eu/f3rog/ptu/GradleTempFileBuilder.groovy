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
        if (hasAndroidPlugin()) {
            androidConfig = """android {
    compileSdkVersion 23
    buildToolsVersion "23.0.2"

    defaultConfig {
        applicationId "com.example"
        minSdkVersion 14
        targetSdkVersion 23
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}
            """
        } else {
            androidConfig = ""
        }


        return """buildscript {
    dependencies {
        repositories {
            mavenLocal()
            mavenCentral()
            jcenter()
        }

        ${mConfig.getClasspaths().collect({ "classpath '$it'" }).join("\n")}
    }
}

${mConfig.getPlugins().collect({ "apply plugin: '$it'" }).join("\n")}

${androidConfig}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    ${mConfig.getDependencies().join("\n")}
}

project.plugins.each {
    print 'Plugin: '
    println it
}
        """
    }

    @Override
    public TempFileBuilder[] getRelatedFiles() {
        return hasAndroidPlugin() ? [new AndroidManifestTempFileBuilder(), new MainActivityTempFileBuilder()] : []
    }

    private boolean hasAndroidPlugin() {
        return mConfig.getPlugins().contains('com.android.application')
    }
}
