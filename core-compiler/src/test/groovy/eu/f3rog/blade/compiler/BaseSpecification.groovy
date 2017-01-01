package eu.f3rog.blade.compiler

import com.google.common.truth.Truth
import com.google.testing.compile.CompileTester
import com.google.testing.compile.JavaSourcesSubjectFactory
import spock.lang.Specification

import eu.f3rog.blade.compiler.BladeProcessor

import javax.tools.JavaFileObject

public abstract class BaseSpecification
        extends Specification {

    protected ITruthWrapper assertFiles(JavaFileObject... f) {
        List<JavaFileObject> files = new ArrayList<>()
        files.addAll(Arrays.asList(f))

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
}