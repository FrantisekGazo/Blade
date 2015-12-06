package eu.f3rog.blade.compiler;

import android.app.Activity;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Extra;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link ActivityInjectorTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class ActivityInjectorTest extends BaseTest {

    @Test
    public void invalidCLass() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_class_with_Extra.toString());
    }

    @Test
    public void invalidField() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E private String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Extra.toString());

        input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E protected String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Extra.toString());

        input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E final String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Extra.toString());
    }

    @Test
    public void one() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E String mExtraString;",
                        "   @$E int mA;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MainActivity_Injector")
                .imports(
                        input, "I",
                        BundleWrapper.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static void inject($I target) {",
                        "       if (target.getIntent() == null || target.getIntent().getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper extras = BundleWrapper.from(target.getIntent().getExtras());",
                        "       target.mExtraString = extras.get(\"<Extra-mExtraString>\", target.mExtraString);",
                        "       target.mA = extras.get(\"<Extra-mA>\", target.mA);",
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
