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

    private static String MVP_MODULE_ERROR_DAGGER_MISSING = "Blade module 'mvp' requires dagger2 dependency!"

    private static String buildGradleClasspath(final String version) {
        return "com.android.tools.build:gradle:${version}"
    }

    private final static def WHERE_DATA = [
            // #gradleToolsVersion, #gradleVersion, #aptClasspath, #apt, #bladeFileType
            ['3.0.0', '4.1', [], [], BladeTempFileBuilder.FileType.JSON],
            ['3.2.0', '4.6', [], [], BladeTempFileBuilder.FileType.JSON],
            ['3.2.1', '4.9', [], [], BladeTempFileBuilder.FileType.JSON],
            ['3.0.0', '4.1', [], [], BladeTempFileBuilder.FileType.YAML],
            ['3.2.0', '4.6', [], [], BladeTempFileBuilder.FileType.YAML],
            ['3.2.1', '4.9', [], [], BladeTempFileBuilder.FileType.YAML]
    ]

    private final static def WHERE_DATA_YAML = WHERE_DATA.findAll { it[4] == BladeTempFileBuilder.FileType.YAML }

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
        projectFolder.addGradleFile(new GradleConfig(gradleToolsVersion)
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
        [gradleToolsVersion, _, _, _, _] << WHERE_DATA_YAML
    }

    @Unroll
    def "fail if non-existing module name used - for #gradleToolsVersion, #bladeFileType"() {
        given:
        projectFolder.addBladeFile(bladeFileType, ["arg", "fake"])
        projectFolder.addGradleFile(new GradleConfig(gradleToolsVersion)
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
    def "fail without blade file - for #gradleToolsVersion"() {
        given:
        projectFolder.addGradleFile(new GradleConfig(gradleToolsVersion)
                .classpaths([buildGradleClasspath(gradleToolsVersion)] + aptClasspath + [bladeClasspath])
                .plugins(["com.android.application"] + apt + ["blade"])
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
        e.getMessage().contains(BladePlugin.ERROR_CONFIG_FILE_IS_MISSING)

        where:
        [gradleToolsVersion, gradleVersion, aptClasspath, apt, _] << WHERE_DATA_YAML
    }

    @Unroll
    def "add correct dependencies - for #gradleToolsVersion, #bladeFileType"() {
        given:
        def bladeModules = ["arg"]
        projectFolder.addBladeFile(bladeFileType, bladeModules)
        projectFolder.addGradleFile(new GradleConfig(gradleToolsVersion)
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
    def "build successfully [EXTRA, arg] modules - for #gradleToolsVersion, #bladeFileType"() {
        given:
        projectFolder.addBladeFile(bladeFileType, ["EXTRA", "arg"]) // also test it's case insensitive
        projectFolder.addGradleFile(new GradleConfig(gradleToolsVersion)
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
    def "fail [mvp] without dagger dependency - for #gradleToolsVersion, #bladeFileType"() {
        given:
        projectFolder.addBladeFile(bladeFileType, ['mvp'])
        projectFolder.addGradleFile(new GradleConfig(gradleToolsVersion)
                .classpaths([buildGradleClasspath(gradleToolsVersion)] + aptClasspath + [bladeClasspath])
                .plugins(["com.android.application"] + apt + ["blade"])
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
        e.getMessage().contains(MVP_MODULE_ERROR_DAGGER_MISSING)

        where:
        [gradleToolsVersion, gradleVersion, aptClasspath, apt, bladeFileType] << WHERE_DATA
    }

    @Unroll
    def "build successfully [mvp] module - for #gradleToolsVersion, #bladeFileType"() {
        given:
        projectFolder.addBladeFile(bladeFileType, ['mvp'])
        projectFolder.addGradleFile(new GradleConfig(gradleToolsVersion)
                .classpaths([buildGradleClasspath(gradleToolsVersion)] + aptClasspath + [bladeClasspath])
                .plugins(["com.android.application"] + apt + ["blade"])
                .dependencies(["com.google.dagger:dagger:2.11"])
                .apDependencies(["com.google.dagger:dagger-compiler:2.11"])
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