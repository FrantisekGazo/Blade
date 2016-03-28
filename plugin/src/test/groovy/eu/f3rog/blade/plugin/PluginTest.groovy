package eu.f3rog.blade.plugin

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import static org.gradle.testkit.runner.TaskOutcome.*

class PluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    private File buildFile

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    @Test
    public void testHelloWorldTask() throws IOException {
        buildFile << """
            task helloWorld {
                doLast {
                    println 'Hello world!'
                }
            }
        """

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("helloWorld")
                .build();

        Assert.assertTrue(result.getOutput().contains("Hello world!"));
        Assert.assertEquals(result.task(":helloWorld").getOutcome(), SUCCESS);
    }

}