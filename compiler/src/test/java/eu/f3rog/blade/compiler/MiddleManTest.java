package eu.f3rog.blade.compiler;

import android.app.Activity;
import android.app.Fragment;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Arg;
import blade.Extra;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link MiddleManTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class MiddleManTest extends BaseTest {

    @Test
    public void extra() {
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

        JavaFileObject expected = generatedFile("blade", "MiddleMan")
                .imports(
                        input, "I",
                        "com.example.MainActivity_Helper", "H"
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static void inject($I target) {",
                        "       $H.inject(target);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void arg() {
        JavaFileObject input = file("com.example", "MyFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        "public class $T extends Fragment {",
                        "",
                        "   @$A String mText;",
                        "   @$A int mNum;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("blade", "MiddleMan")
                .imports(
                        input, "F",
                        "com.example.MyFragment_Helper", "H"
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static void inject($F target) {",
                        "       $H.inject(target);",
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
