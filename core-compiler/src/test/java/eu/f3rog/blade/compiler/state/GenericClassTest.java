package eu.f3rog.blade.compiler.state;

import android.os.Bundle;

import org.junit.Test;

import java.io.Serializable;

import javax.tools.JavaFileObject;

import blade.State;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link GenericClassTest}
 *
 * @author FrantisekGazo
 * @version 2016-04-10
 */
public final class GenericClassTest extends BaseTest {

    @Test
    public void genericClass() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T<T> {",
                        "",
                        "   @$S String mText;",
                        "   @$S int mNumber;",
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
                        "abstract class $T {",
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
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void genericField() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        State.class, "S",
                        Serializable.class
                )
                .body(
                        "public class $T<T extends Serializable> {",
                        "",
                        "   @$S T mData;",
                        "   @$S int mNumber;",
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
                        "abstract class $T {",
                        "",
                        "   public static void saveState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-mData>\", target.mData);",
                        "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "   public static void restoreState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       target.mData = bundleWrapper.get(\"<Stateful-mData>\", target.mData);",
                        "       target.mNumber = bundleWrapper.get(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}
