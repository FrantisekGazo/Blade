package eu.f3rog.blade.weaving.util.task;

import org.gradle.api.GradleException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;


class TransformationAction {

    private File destinationDir;
    private IClassTransformer transformation;

    private List<File> sources = new LinkedList<>();
    private List<CtClass> loadedClasses = new LinkedList<>();
    private Collection<File> classpath = new LinkedList<>();

    public TransformationAction(File destinationDir, Collection<File> sources, Collection<File> classpath, IClassTransformer transformation) {
        this.destinationDir = destinationDir;
        this.sources.addAll(sources);
        this.classpath.addAll(classpath);
        this.transformation = transformation;
    }

    public boolean execute() {
        // no op if no transformation defined
        if (this.transformation == null) {
            System.out.println("No transformation defined for this task");
            return false;
        }

        if (this.sources == null || this.sources.size() == 0) {
            System.out.println("No source files.");
            return false;
        }

        if (destinationDir == null) {
            System.out.println("No destination directory set");
            return false;
        }

        try {
            final ClassPool pool = createPool();

            this.process(pool, this.loadedClasses);
        } catch (Exception e) {
            throw new GradleException("Could not execute transformation", e);
        }

        return true;
    }

    private ClassPool createPool() throws NotFoundException, IOException {
        final ClassPool pool = new AnnotationLoadingClassPool();

        // set up the classpath for the classpool
        if (classpath != null) {
            for (File f : this.classpath) {
                pool.appendClassPath(f.toString());
            }
        }

        // add the files to process
        for (File f : this.sources) {
            if (!f.isDirectory()) {
                loadedClasses.add(loadClassFile(pool, f));
            }
        }
        return pool;
    }

    public void process(ClassPool pool, Collection<CtClass> classes) {
        for (CtClass clazz : classes) {
            processFile(pool, clazz);
        }
    }

    public void processFile(ClassPool pool, CtClass clazz) {
        try {
            if (transformation.shouldTransform(clazz)) {
                clazz.defrost();
                transformation.applyTransformations(clazz);
                clazz.writeFile(this.destinationDir.toString());
            }
        } catch (Exception e) {
            throw new GradleException("An error occurred while trying to process class file ", e);
        }
    }

    private CtClass loadClassFile(ClassPool pool, File classFile) throws IOException {
        // read the file first to get the classname
        // much easier than trying to extrapolate from the filename (i.e. with anonymous classes etc.)
        InputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(classFile)));
        CtClass clazz = pool.makeClass(stream);

        stream.close();

        return clazz;
    }

    /**
     * This class loader will load annotations encountered in loaded classes
     * using the pool itself.
     *
     * @see <a href="https://github.com/jboss-javassist/javassist/pull/18">Javassist issue 18</a>
     */
    private static class AnnotationLoadingClassPool extends ClassPool {
        public AnnotationLoadingClassPool() {
            super(true);
        }

        @Override
        public ClassLoader getClassLoader() {
            return new Loader(this);
        }
    }
}
