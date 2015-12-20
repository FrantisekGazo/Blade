package eu.f3rog.blade.compiler;

import android.os.Bundle;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.State;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link StateTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class StateTest extends BaseTest {

    @Test
    public void invalidField() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$S private String mText;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(State.class.getSimpleName()));

        input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$S protected String mText;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(State.class.getSimpleName()));

        input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$S final String mText;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(State.class.getSimpleName()));
    }

    @Test
    public void two() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$S String mText;",
                        "   @$S int mNumber;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyClass_Helper")
                .imports(
                        input, "I",
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E"
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static void saveState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-mText>\", target.mText);",
                        "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "   public static void restoreState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       target.mText = bundleWrapper.get(\"<Stateful-mText>\", target.mText);",
                        "       target.mNumber = bundleWrapper.get(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
