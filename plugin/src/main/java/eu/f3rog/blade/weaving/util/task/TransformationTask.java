package eu.f3rog.blade.weaving.util.task;

import groovy.lang.Closure;
import java.util.Collection;
import javassist.build.IClassTransformer;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Paths;

public class TransformationTask extends DefaultTask {
    private Object destinationDir;
    private Object classesDir;

    private IClassTransformer transformation;
    private FileCollection classpath;

    public TransformationTask() {
        // empty classpath
        this.classpath = this.getProject().files();

        this.destinationDir = Paths.get(this.getProject().getBuildDir().toString(), "transformations", this.getName()).toFile();
    }

    @OutputDirectory
    public File getDestinationDir() {
        return this.getProject().file(destinationDir);
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    public IClassTransformer getTransformation() {
        return transformation;
    }

    public void setTransformation(IClassTransformer transformation) {
        this.transformation = transformation;
    }

    @InputFiles
    public FileCollection getClasspath() {
        return this.classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @InputFiles
    public FileCollection getSources() {
        if (this.classesDir == null) {
            return this.getProject().files();
        }

        ConfigurableFileTree result = this.getProject().fileTree(this.classesDir);
        result.include("**/*.class");

        return result;
    }

    public void transform(Closure closure) {
        this.transformation = new GroovyClassTransformation(closure);
    }

    public void where(Closure closure) {
        this.transformation = new GroovyClassTransformation(null, closure);
    }

    public void from(Object dir) {
        this.classesDir = dir;
    }

    public void into(Object dir) {
        this.destinationDir = dir;
    }

    public void eachFile(Closure closure) {
        closure.call(this.getSources().getFiles());
    }

    @TaskAction
    protected void exec() {
        Collection<File> classPath = this.classpath.getFiles();
        if (classesDir != null) {
            classPath.add(this.getProject().file(classesDir));
        }

        boolean workDone = new eu.f3rog.blade.weaving.util.task.TransformationAction(
                this.getDestinationDir(),
                this.getSources().getFiles(),
                classPath,
                this.transformation
        ).execute();

        this.setDidWork(workDone);
    }

}
