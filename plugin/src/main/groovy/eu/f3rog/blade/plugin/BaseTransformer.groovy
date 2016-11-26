package eu.f3rog.blade.plugin

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import eu.f3rog.blade.weaving.util.IWeaver
import groovy.io.FileType
import javassist.ClassPool
import javassist.LoaderClassPath

import java.util.jar.JarFile

/**
 * Gradle plugin with all necessary functionality for class transformation (using bytecode weaving)
 */
public abstract class BaseTransformer
        extends Transform {

    private boolean mDebug

    public BaseTransformer(boolean debug) {
        mDebug = debug
    }

    boolean isDebug() {
        return mDebug
    }

    @Override
    Set<ContentType> getInputTypes() {
        return ImmutableSet.<ContentType> of(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<Scope> getScopes() {
        return Sets.immutableEnumSet(Scope.PROJECT)
    }

    @Override
    Set<Scope> getReferencedScopes() {
        return Sets.immutableEnumSet(Scope.EXTERNAL_LIBRARIES)
    }

    @Override
    boolean isIncremental() {
        return false
    }

    abstract IWeaver getWeaver(boolean debug)

    @Override
    void transform(Context context,
                   Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        String name = getName()
        log " ------------------------------------------> "
        log " * ${name} "
        log " ------------------------------------------> "

        long tic = System.currentTimeMillis()

        IWeaver weaver = getWeaver(mDebug)

        String path = getOutputDir(outputProvider).absolutePath
        log " * Output path: ${path}"

        // Find all the class names
        def inputClassNames = getClassNames(inputs)
        //def referencedClassNames = getClassNames(referencedInputs)
        def allClassNames = inputClassNames //merge(inputClassNames, referencedClassNames)

        // Create and populate the Javassist class pool
        ClassPool classPool = createClassPool(inputs, referencedInputs)
        log " * ClassPool initialized with ${allClassNames.size()} classes"

        def allCtClasses = allClassNames
                .collect { classPool.getCtClass(it) }

        weaver.weave(classPool, allCtClasses)

        // all classes need to we written
        for (ctClass in allCtClasses) {
            ctClass.writeFile(path)
        }

        long toc = System.currentTimeMillis()
        log " * Transform time: ${toc - tic} ms"

        log " <------------------------------------------ "
        log " * ${name} "
        log " <------------------------------------------ "
    }

    /**
     * Creates and populates the Javassist class pool.
     *
     * @param inputs the inputs provided by the Transform API
     * @param referencedInputs the referencedInputs provided by the Transform API
     * @return the populated ClassPool instance
     */
    private ClassPool createClassPool(Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs) {
        // Don't use ClassPool.getDefault(). Doing consecutive builds in the same run (e.g. debug+release)
        // will use a cached object and all the classes will be frozen.
        ClassPool classPool = new ClassPool(null)
        classPool.appendSystemPath()
        classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()))

        inputs.each {
            it.directoryInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }

            it.jarInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }
        }

        referencedInputs.each {
            it.directoryInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }

            it.jarInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }
        }

        return classPool
    }

    private static Set<String> getClassNames(Collection<TransformInput> inputs) {
        Set<String> classNames = new HashSet<String>()

        inputs.each {
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                it.file.eachFileRecurse(FileType.FILES) {
                    if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                        def className =
                                it.absolutePath.substring(
                                        dirPath.length() + 1,
                                        it.absolutePath.length() - SdkConstants.DOT_CLASS.length()
                                ).replace(File.separatorChar, '.' as char)
                        classNames.add(className)
                    }
                }
            }

            it.jarInputs.each {
                def jarFile = new JarFile(it.file)
                jarFile.entries().findAll {
                    !it.directory && it.name.endsWith(SdkConstants.DOT_CLASS)
                }.each {
                    def path = it.name
                    def className = path.substring(0, path.length() - SdkConstants.DOT_CLASS.length())
                            .replace(File.separatorChar, '.' as char)
                    classNames.add(className)
                }
            }
        }
        return classNames
    }

    private File getOutputDir(TransformOutputProvider outputProvider) {
        return outputProvider.getContentLocation(
                getName(), getInputTypes(), getScopes(), Format.DIRECTORY)
    }

    private static Set<String> merge(Set<String> set1, Set<String> set2) {
        Set<String> merged = new HashSet<String>()
        merged.addAll(set1)
        merged.addAll(set2)
        return merged;
    }

    private void log(String s) {
        if (mDebug) {
            System.out.println(s)
        }
    }
}
