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
 * @version 2015-11-19
 */
public class BaseTest {

    protected CompileTester assertFiles(JavaFileObject... f) {
        List<JavaFileObject> files = new ArrayList<>();
        files.addAll(Arrays.asList(f));

        return Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(files)
                .processedWith(new BladeProcessor());
    }

    protected static String join(String... lines) {
        return Joiner.on("\n").join(lines);
    }

}
