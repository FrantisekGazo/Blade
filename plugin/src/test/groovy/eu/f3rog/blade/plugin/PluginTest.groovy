package eu.f3rog.blade.plugin

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginTest extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        testProjectDir.newFolder("src", "main")
        testProjectDir.newFile('src/main/AndroidManifest.xml') << """<?xml version="1.0" encoding="utf-8"?>
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
        testProjectDir.newFolder("src", "main", "java", "com", "example")
        testProjectDir.newFile('src/main/java/com/example/MainActivity.java') << """
            package com.example;

            import android.app.Activity;

            public class MainActivity extends Activity {}
        """
    }

    @Unroll
    def "successfully build -> Blade #bladeVersion"() {
        given:
        buildFile << """
            buildscript {
                dependencies {
                    repositories {
                        mavenCentral()
                        jcenter()

                        // NOTE: This is only needed when developing the plugin!
                        mavenLocal()
                    }

                    classpath 'com.android.tools.build:gradle:1.5.0'
                    classpath 'eu.f3rog.blade:plugin:${bladeVersion}'
                }
            }

            apply plugin: 'com.android.application'
            apply plugin: 'blade'

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

            dependencies {
                compile fileTree(dir: 'libs', include: ['*.jar'])
            }
        """

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(':build')
                .build()

        then:
        result.task(":build").outcome == SUCCESS

        where:
        bladeVersion << ['2.1.0']
    }

    @Unroll
    def "fail without android plugin -> Blade #bladeVersion"() {
        given:
        buildFile << """
            buildscript {
                dependencies {
                    repositories {
                        mavenCentral()
                        jcenter()

                        // NOTE: This is only needed when developing the plugin!
                        mavenLocal()
                    }

                    classpath 'com.android.tools.build:gradle:1.5.0'
                    classpath 'eu.f3rog.blade:plugin:${bladeVersion}'
                }
            }

            apply plugin: 'blade'
        """

        when:
        Exception e = null
        try {
            BuildResult result = GradleRunner.create()
                    .withProjectDir(testProjectDir.root)
                    .withArguments(':build')
                    .build()
        } catch (Exception ex) {
            e = ex
        }

        then:
        e != null
        e.getMessage().contains("'com.android.application' or 'com.android.library' plugin required.")

        where:
        bladeVersion << ['2.1.0']
    }
}