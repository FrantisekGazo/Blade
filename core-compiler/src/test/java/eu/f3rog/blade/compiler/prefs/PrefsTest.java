package eu.f3rog.blade.compiler.prefs;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Prefs;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;

import static eu.f3rog.blade.compiler.util.File.file;

/**
 * Class {@link PrefsTest}
 *
 * @author FrantisekGazo
 * @version 2016-08-09
 */
public final class PrefsTest extends BaseTest {

    @Test
    public void invalidClass() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public class $T {",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .failsToCompile()
                .withErrorContaining(PrefsErrorMsg.Invalid_type_with_Prefs);
    }

    @Test
    public void invalidAnnotation() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public @interface $T {",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .failsToCompile()
                .withErrorContaining(PrefsErrorMsg.Invalid_type_with_Prefs);
    }

    @Test
    public void invalidEnum() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public enum $T {",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .failsToCompile()
                .withErrorContaining(PrefsErrorMsg.Invalid_type_with_Prefs);
    }

    @Test
    public void validInterface() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public interface $T {",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .compilesWithoutError();
    }
}
