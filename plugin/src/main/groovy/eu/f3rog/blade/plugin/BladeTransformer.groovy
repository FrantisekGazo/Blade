package eu.f3rog.blade.plugin

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import eu.f3rog.blade.weaving.BladeWeaver
import groovy.io.FileType
import javassist.ClassPool
import javassist.CtClass
import javassist.LoaderClassPath

import java.util.jar.JarFile

public class BladeTransformer extends Transform {

    private boolean mDebug

    public BladeTransformer(boolean debug) {
        mDebug = debug
    }

    @Override
    String getName() {
        return "BladeTransformer"
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

    @Override
    void transform(Context context,
                   Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        log " * Bl@de >------------------------------------------"

        long tic = System.currentTimeMillis()

        BladeWeaver weaver = new BladeWeaver(mDebug);

        String path = getOutputDir(outputProvider).absolutePath
        log " * Output path: ${path}"

        // Find all the class names
        def inputClassNames = getClassNames(inputs)
        def referencedClassNames = getClassNames(referencedInputs)
        def allClassNames = merge(inputClassNames, referencedClassNames)

        // Create and populate the Javassist class pool
        ClassPool classPool = createClassPool(inputs, referencedInputs)
        log " * ClassPool initialized with ${inputClassNames.size()} input classes and ${referencedClassNames.size()} reference classes"

        // find all helper classes
        def allHelperClasses = inputClassNames
                .findAll { it.endsWith('_Helper') } //  only helper classes
                .collect { classPool.getCtClass(it) } // get CtClasses

        // weave all classes with helper class
        for (helperClass in allHelperClasses) {
            String intoClassName = helperClass.getName().replace("_Helper", "")

            if (!inputClassNames.contains(intoClassName)) {
                continue
            }

            CtClass intoClass = classPool.getCtClass(intoClassName)

            log " * Weaving '${helperClass.getName()}' into '${intoClass.getName()}'"

            weaver.weave(helperClass, intoClass)

            // write weaved class
            intoClass.writeFile(path)
            inputClassNames.remove(intoClass.getName())
        }

        // write remaining classes
        for (className in inputClassNames) {
            CtClass ctClass = classPool.get(className)
            ctClass.writeFile(path)
        }

        long toc = System.currentTimeMillis()
        log " * Blade Transform time: ${toc - tic} milliseconds"

        log " * Bl@de <------------------------------------------"
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
                'blade', getInputTypes(), getScopes(), Format.DIRECTORY)
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