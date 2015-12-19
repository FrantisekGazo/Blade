package eu.f3rog.blade.compiler;

import android.app.Fragment;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Arg;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link FragmentInjectorTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class FragmentInjectorTest extends BaseTest {

    @Test
    public void invalidCLass() {
        JavaFileObject input = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$A String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_class_with_Arg.toString());
    }

    @Test
    public void invalidField() {
        JavaFileObject input = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        "public class $T extends Fragment {",
                        "",
                        "   @$A private String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Arg_field.toString());

        input = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        "public class $T extends Fragment {",
                        "",
                        "   @$A protected String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Arg_field.toString());

        input = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        "public class $T extends Fragment {",
                        "",
                        "   @$A final String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Arg_field.toString());
    }

    @Test
    public void one() {
        JavaFileObject input = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        "public class $T extends Fragment {",
                        "",
                        "   @$A String mExtraString;",
                        "   @$A int mA;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MainFragment_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static void inject($I target) {",
                        "       if (target.getArguments() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper args = BundleWrapper.from(target.getArguments());",
                        "       target.mExtraString = args.get(\"<Extra-mExtraString>\", target.mExtraString);",
                        "       target.mA = args.get(\"<Extra-mA>\", target.mA);",
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
