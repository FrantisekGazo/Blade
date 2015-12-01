package eu.f3rog.automat.compiler;

import android.app.Activity;

import org.junit.Test;

import javax.tools.JavaFileObject;

import eu.f3rog.automat.Extra;

import static eu.f3rog.automat.compiler.util.File.file;
import static eu.f3rog.automat.compiler.util.File.generatedFile;

/**
 * Class {@link InjectorTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class InjectorTest extends BaseTest {
    public final static String[] SUPPORTED = {
            "com.example.$I",
    };

    @Test
    public void test1() {
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

        JavaFileObject expected = generatedFile("automat", "Injector")
                .imports(
                        input, "I",
                        "com.example.MainActivity_Injector"
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static void inject($I target) {",
                        "       $I_Injector.inject(target);",
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
