package eu.f3rog.blade.compiler;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaSourcesSubjectFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaFileObject;

/**
 * Class {@link BaseTest}
 *
 * @author FrantisekGazo
 */
public abstract class BaseTest {

    protected ITruthWrapper assertFiles(JavaFileObject... f) {
        List<JavaFileObject> files = new ArrayList<>();
        files.addAll(Arrays.asList(f));

        return new TruthWrapper(files);
    }

    protected static String join(String... lines) {
        return Joiner.on("\n").join(lines);
    }

    public interface ITruthWrapper {
        CompileTester with(BladeProcessor.Module... processorModules);
    }

    private static class TruthWrapper implements ITruthWrapper {

        private final List<JavaFileObject> mFiles;

        public TruthWrapper(List<JavaFileObject> files) {
            mFiles = files;
        }

        @Override
        public CompileTester with(BladeProcessor.Module... processorModules) {
            return Truth.assert_()
                    .about(JavaSourcesSubjectFactory.javaSources())
                    .that(mFiles)
                    .processedWith(new BladeProcessor(processorModules));
        }

    }

}
