package eu.f3rog.blade.compiler

import com.google.common.truth.Truth
import com.google.testing.compile.CompileTester
import com.google.testing.compile.JavaSourcesSubjectFactory
import spock.lang.Specification

import eu.f3rog.blade.compiler.BladeProcessor

import javax.tools.JavaFileObject
import javax.tools.StandardLocation

public abstract class BaseSpecification
        extends Specification {

    protected ITruthWrapper assertFiles(Object... files) {
        List<JavaFileObject> allFiles = new ArrayList<>()
        for (file in files) {
            if (file instanceof JavaFileObject) {
                allFiles.add((JavaFileObject) file)
            }
        }

        return new TruthWrapper(allFiles)
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
                                                            Object... inputFiles) {
        try {
            assertFiles(inputFiles)
                    .with(module)
                    .compilesWithoutError()
                    .and()
                    .generatesFileNamed(StandardLocation.CLASS_OUTPUT, pkg, className + '.class')
            return false
        } catch (AssertionError e) {
            return e.getMessage().contains("Did not find a generated file corresponding to ${className}.class in package ${pkg}")
        }
    }
}