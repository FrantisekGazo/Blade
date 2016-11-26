package eu.f3rog.blade.plugin

import org.junit.rules.TemporaryFolder

public class ProjectFolder extends TemporaryFolder {

    public File addBladeFile(List<String> modules) {
        File file = this.newFile('blade.json')
        file << " { "
        file << "\"debug\": false,"
        file << "\"modules\": ["
        for (int i = 0; i < modules.size(); i++) {
            if (i > 0) file << ", "
            file << "\"${modules.get(i)}\""
        }
        file << " ] "
        file << " } "
        return file
    }

    public File addGradleBuildFile(String gradleToolsVersion, String bladeVersion, boolean android, deps = []) {
        File file = this.newFile('build.gradle')
        file << """
            buildscript {
                dependencies {
                    repositories {
                        mavenCentral()
                        jcenter()

                        // NOTE: This is only needed when developing the plugin!
                        mavenLocal()
                    }

                    classpath 'com.android.tools.build:gradle:${gradleToolsVersion}'
                    classpath 'eu.f3rog.blade:plugin:${bladeVersion}'
                }
            }
        """

        // apply Android Project plugin
        if (android) {
            file << """
                apply plugin: 'com.android.application'
            """
        }
        // apply Blade plugin
        file << """
            apply plugin: 'blade'
        """
        // add android setup
        if (android) {
            addManifestFile()
            addMainActivityFile()
            file << """
                android {
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

            file << """
                dependencies {
                    compile fileTree(dir: 'libs', include: ['*.jar'])
            """
            for (dep in deps) {
                file << dep
            }
            file << """
                }
            """
        }

        // add extra tasks
        // this will list all applied plugins at the beginning
        file << """
            project.plugins.each {
                print 'Plugin: '
                println it
            }
        """

        return file
    }

    private File addManifestFile() {
        this.newFolder("src", "main")
        File file = this.newFile('src/main/AndroidManifest.xml')
        file << """<?xml version="1.0" encoding="utf-8"?>
            <manifest package="com.example"
                      xmlns:android="http://schemas.android.com/apk/res/android">

                <application>
                    <activity android:name=".MainActivity">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN"/>

                            <category android:name="android.intent.category.LAUNCHER"/>
                        </intent-filter>
                    </activity>
                </application>

            </manifest>
        """
        return file
    }

    private File addMainActivityFile() {
        this.newFolder("src", "main", "java", "com", "example")
        File file = this.newFile('src/main/java/com/example/MainActivity.java')
        file << """package com.example;

            import android.app.Activity;

            public class MainActivity extends Activity {}
        """
        return file
    }
}