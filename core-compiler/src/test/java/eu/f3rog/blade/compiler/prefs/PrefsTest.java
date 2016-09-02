package eu.f3rog.blade.compiler.prefs;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Prefs;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link PrefsTest}
 *
 * @author FrantisekGazo
 * @version 2016-08-09
 */
public final class PrefsTest extends BaseTest {

    @Test
    public void invalidUsageClass() {
        JavaFileObject input = file("com.example", "Test")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public class $T {}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .failsToCompile()
                .withErrorContaining(PrefsErrorMsg.Invalid_type_with_Prefs);
    }

    @Test
    public void invalidUsageAnnotation() {
        JavaFileObject input = file("com.example", "Test")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public @interface $T {}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .failsToCompile()
                .withErrorContaining(PrefsErrorMsg.Invalid_type_with_Prefs);
    }

    @Test
    public void invalidUsageEnum() {
        JavaFileObject input = file("com.example", "Test")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public enum $T {}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .failsToCompile()
                .withErrorContaining(PrefsErrorMsg.Invalid_type_with_Prefs);
    }

    @Test
    public void validUsageInterface() {
        JavaFileObject input = file("com.example", "Test")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public interface $T {}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .compilesWithoutError();
    }

    @Test
    public void nameOfGeneratedClass() {
        JavaFileObject input = file("com.example", "Test")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public interface $T {}"
                );

        JavaFileObject expected = generatedFile("com.example", "Test_Prefs")
                .imports(
                )
                .body(
                        "public class $T {}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void generatedConstructor() {
        JavaFileObject input = file("com.example", "Test")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public interface $T {}"
                );

        JavaFileObject expected = generatedFile("com.example", "Test_Prefs")
                .imports(
                )
                .body(
                        "public class $T {",
                        "",
                        "   public $T() {",
                        "       TODO",
                        "   }",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void stringProperty() {
        JavaFileObject input = file("com.example", "Test")
                .imports(
                        Prefs.class, "P"
                )
                .body(
                        "@$P",
                        "public interface $T {",
                        "",
                        "   String text;",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "Test_Prefs")
                .imports(
                )
                .body(
                        "public class $T {",
                        "",
                        "   public String getText() {",
                        "       return TODO",
                        "   }",
                        "",
                        "   public String getText(String defaultValue) {",
                        "       return TODO",
                        "   }",
                        "",
                        "   public String setText(final String text) {",
                        "       return TODO",
                        "   }",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PREFS)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}
