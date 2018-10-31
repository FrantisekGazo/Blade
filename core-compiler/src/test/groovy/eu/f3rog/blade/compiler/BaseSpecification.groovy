package eu.f3rog.blade.compiler

import com.google.common.truth.Truth
import com.google.testing.compile.CompileTester
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory
import eu.f3rog.blade.compiler.name.ClassNames
import eu.f3rog.blade.compiler.util.JavaFile
import org.codehaus.groovy.runtime.InvokerInvocationException
import spock.lang.Shared
import spock.lang.Specification

import eu.f3rog.blade.compiler.BladeProcessor

import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject

public abstract class BaseSpecification
        extends Specification {

    protected ITruthWrapper assertFiles(JavaFileObject... f) {
        List<JavaFileObject> files = new ArrayList<>()
        files.add(androidxActivity)
        files.add(androidxFragment)
        files.add(supportActivity)
        files.add(supportFragment)
        for (file in f) {
            files.add(file)
        }

        return new TruthWrapper(files)
    }

    public interface ITruthWrapper {
        CompileTester with(BladeProcessor.Module... processorModules)
    }

    private static class TruthWrapper implements ITruthWrapper {

        private final List<JavaFileObject> mFiles

        public TruthWrapper(final List<JavaFileObject> files) {
            mFiles = files
        }

        @Override
        public CompileTester with(final BladeProcessor.Module... processorModules) {
            return Truth.assert_()
                    .about(JavaSourcesSubjectFactory.javaSources())
                    .that(mFiles)
                    .processedWith(new BladeProcessor(processorModules))
        }
    }

    protected boolean compilesWithoutErrorAndDoesntGenerate(String pkg,
                                                            String className,
                                                            BladeProcessor.Module module,
                                                            JavaFileObject... inputFiles) {
        def result = assertFiles(inputFiles)
                .with(module)
                .compilesWithoutError()

        List<SimpleJavaFileObject> sources = result.compilation.sourceFiles
        String target = pkg.replace(".", "/") + "/" + className

        for (source in sources) {
            if (source.toUri().path.contains(target)) {
                return false
            }
        }
        return true
    }

    @Shared JavaFileObject androidxFragment = fragment(ClassNames.AndroidxFragment)
    @Shared JavaFileObject supportFragment = fragment(ClassNames.SupportFragment)
    @Shared JavaFileObject androidxActivity = activity(ClassNames.AndroidxActivity)
    @Shared JavaFileObject supportActivity = activity(ClassNames.SupportActivity)

    @Shared fragmentClasses = [
            ["Support Fragment", supportFragment],
            ["AndroidX Fragment", androidxFragment]
    ]
    @Shared activityClasses = [
            ["Support Activity", supportActivity],
            ["AndroidX Activity", androidxActivity]
    ]

    private static JavaFileObject fragment(ClassNames className) {
        return JavaFile.newFile(className.getPackageName(), className.getClassName(), """
            public class #T {
                public final android.os.Bundle getArguments() {
                    throw new RuntimeException("Stub!");
                }
                
                public void setArguments(android.os.Bundle args) {
                    throw new RuntimeException("Stub!");
                }
            }
            """
        )
    }

    private static JavaFileObject activity(ClassNames className) {
        return JavaFile.newFile(className.getPackageName(), className.getClassName(), """
            public class #T {
            }
            """
        )
    }

    private static JavaFileObject emptyClass(ClassNames className) {
        return JavaFile.newFile(className.getPackageName(), className.getClassName(), """
            public class #T {
            }
            """
        )
    }
}