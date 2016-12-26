package eu.f3rog.blade.plugin

import eu.f3rog.ptu.BladeTempFileBuilder
import eu.f3rog.ptu.GradleConfig
import eu.f3rog.ptu.TempProjectFolder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS


final class BladePluginSpecification
        extends Specification {

    private static final String APT_CLASSPATH = "com.neenbedankt.gradle.plugins:android-apt:1.8"
    private static final String APT_PLUGIN = "com.neenbedankt.android-apt"

    private static String buildGradleClasspath(final String version) {
        return "com.android.tools.build:gradle:${version}"
    }

    private final static def WHERE_DATA = [
            // #gradleToolsVersion, #gradleVersion, #aptClasspath, #apt, #bladeFileType
            ['1.5.0', '2.9', [APT_CLASSPATH], [APT_PLUGIN], BladeTempFileBuilder.FileType.JSON],
            ['2.0.0', '2.10', [APT_CLASSPATH], [APT_PLUGIN], BladeTempFileBuilder.FileType.JSON],
            ['2.2.0', '2.14.1', [], [], BladeTempFileBuilder.FileType.JSON],
            ['1.5.0', '2.9', [APT_CLASSPATH], [APT_PLUGIN], BladeTempFileBuilder.FileType.YAML],
            ['2.0.0', '2.10', [APT_CLASSPATH], [APT_PLUGIN], BladeTempFileBuilder.FileType.YAML],
            ['2.2.0', '2.14.1', [], [], BladeTempFileBuilder.FileType.YAML]
    ]

    @Rule
    final TempProjectFolder projectFolder = new TempProjectFolder()

    private String bladeVersion
    private String bladeGroupId
    private String bladeClasspath

    def setup() {
        bladeGroupId = BladePlugin.LIB_GROUP_ID
        bladeVersion = BladePlugin.LIB_VERSION
        bladeClasspath = "${bladeGroupId}:plugin:${bladeVersion}"
    }

    @Unroll
    def "fail without android plugin - for #gradleToolsVersion"() {
        given:
        projectFolder.addGradleFile(new GradleConfig()
                .classpaths([buildGradleClasspath(gradleToolsVersion), bladeClasspath])
                .plugins(["blade"])
        )

        when:
        Exception e = null
        try {
            GradleRunner.create()
                    .withProjectDir(projectFolder.root)
                    .withArguments(':build')
                    .build()
        } catch (Exception ex) {
            e = ex
        }

        then:
        e != null
        e.getMessage().contains(BladePlugin.ERROR_ANDROID_PLUGIN_REQUIRED)

        where:
        [gradleToolsVersion, _, _, _, _] << WHERE_DATA[0..2]
    }

    @Unroll
    def "fail if non-existing module name used - for #gradleToolsVersion, #bladeFileType"() {
        given:
        projectFolder.addBladeFile(bladeFileType, ["arg", "fake"])
        projectFolder.addGradleFile(new GradleConfig()
                .classpaths([buildGradleClasspath(gradleToolsVersion), bladeClasspath])
                .plugins(["com.android.application", "blade"])
        )

        when:
        Exception e = null
        try {
            GradleRunner.create()
                    .withGradleVersion(gradleVersion)
                    .withProjectDir(projectFolder.root)
                    .withArguments(':build')
                    .build()
        } catch (Exception ex) {
            e = ex
        }

        then:
        e != null
        e.getMessage().contains(String.format(BladePlugin.ERROR_MODULE_DOES_NOT_EXIST, "fake"))

        where:
        [gradleToolsVersion, gradleVersion, _, _, bladeFileType] << WHERE_DATA
    }

    @Unroll
    def "fail if apt is not applied in gradle <2.2.0 - for #gradleToolsVersion, #bladeFileType"() {
        given:
        projectFolder.addBladeFile(bladeFileType, ["arg"])
        projectFolder.addGradleFile(new GradleConfig()
                .classpaths([buildGradleClasspath(gradleToolsVersion), bladeClasspath])
                .plugins(["com.android.application", "blade"])
        )

        when:
        Exception e = null
        try {
            GradleRunner.create()
                    .withGradleVersion(gradleVersion)
                    .withProjectDir(projectFolder.root)
                    .withArguments(':build')
                    .build()
        } catch (Exception ex) {
            e = ex
        }

        then:
        e != null
        e.getMessage().contains(BladePlugin.ERROR_APT_MISSING)

        where:
        [gradleToolsVersion, gradleVersion, _, _, bladeFileType] << WHERE_DATA[0..1] + WHERE_DATA[3..4]
    }

    @Unroll
    def "add correct dependencies - for #gradleToolsVersion, #bladeFileType"() {
        given:
        def bladeModules = ["arg"]
        projectFolder.addBladeFile(bladeFileType, bladeModules)
        projectFolder.addGradleFile(new GradleConfig()
                .classpaths([buildGradleClasspath(gradleToolsVersion)] + aptClasspath + [bladeClasspath])
                .plugins(["com.android.application"] + apt + ["blade"])
        )

        when:
        BuildResult result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectFolder.root)
                .withArguments('dependencies')
                .build()

        then:
        // check Blade core dependencies
        result.output.contains("$bladeGroupId:core:${bladeVersion}")
        result.output.contains("$bladeGroupId:core-compiler:${bladeVersion}")
        // check other Blade module dependencies
        for (module in BladePlugin.LIB_MODULES) {
            result.output.contains("$bladeGroupId:${module}:${bladeVersion}") == bladeModules.contains(module)
            result.output.contains("$bladeGroupId:${module}-compiler:${bladeVersion}") == bladeModules.contains(module)
        }
        // check plugin
        result.output.contains("eu.f3rog.blade.plugin.BladePlugin")

        where:
        [gradleToolsVersion, gradleVersion, aptClasspath, apt, bladeFileType] << WHERE_DATA
    }

    @Unroll
    def "build successfully without blade file - for #gradleToolsVersion"() {
        given:
        projectFolder.addGradleFile(new GradleConfig()
                .classpaths([buildGradleClasspath(gradleToolsVersion)] + aptClasspath + [bladeClasspath])
                .plugins(["com.android.application"] + apt + ["blade"])
                .dependencies(["compile 'com.google.dagger:dagger:2.0.2'"]) // required for 'mvp' module
        )

        when:
        BuildResult result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectFolder.root)
                .withArguments(':build')
                .build()

        then:
        result.task(":build").outcome == SUCCESS
        result.task(":transformClassesWithBladeForDebug").outcome == SUCCESS
        result.task(":transformClassesWithBladeForRelease").outcome == SUCCESS

        where:
        [gradleToolsVersion, gradleVersion, aptClasspath, apt, _] << WHERE_DATA[0..2]
    }

    @Unroll
    def "build successfully [EXTRA, arg] modules - for #gradleToolsVersion, #bladeFileType"() {
        given:
        projectFolder.addBladeFile(bladeFileType, ["EXTRA", "arg"]) // also test it's case insensitive
        projectFolder.addGradleFile(new GradleConfig()
                .classpaths([buildGradleClasspath(gradleToolsVersion)] + aptClasspath + [bladeClasspath])
                .plugins(["com.android.application"] + apt + ["blade"])
        )

        when:
        BuildResult result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectFolder.root)
                .withArguments(':build')
                .build()

        then:
        result.task(":build").outcome == SUCCESS
        result.task(":transformClassesWithBladeForDebug").outcome == SUCCESS
        result.task(":transformClassesWithBladeForRelease").outcome == SUCCESS

        where:
        [gradleToolsVersion, gradleVersion, aptClasspath, apt, bladeFileType] << WHERE_DATA
    }

    @Unroll
    def "build successfully [mvp] module - for #gradleToolsVersion, #bladeFileType"() {
        given:
        projectFolder.addBladeFile(bladeFileType, ['mvp'])
        projectFolder.addGradleFile(new GradleConfig()
                .classpaths([buildGradleClasspath(gradleToolsVersion)] + aptClasspath + [bladeClasspath])
                .plugins(["com.android.application"] + apt + ["blade"])
                .dependencies(["compile 'com.google.dagger:dagger:2.0.2'"])
        )

        when:
        BuildResult result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectFolder.root)
                .withArguments(':build')
                .build()

        then:
        result.task(":build").outcome == SUCCESS
        result.task(":transformClassesWithBladeForDebug").outcome == SUCCESS
        result.task(":transformClassesWithBladeForRelease").outcome == SUCCESS

        where:
        [gradleToolsVersion, gradleVersion, aptClasspath, apt, bladeFileType] << WHERE_DATA
    }
}