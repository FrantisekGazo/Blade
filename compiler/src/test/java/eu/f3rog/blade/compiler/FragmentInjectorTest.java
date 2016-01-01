package eu.f3rog.blade.compiler;

import android.app.Fragment;
import android.content.Context;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Arg;
import eu.f3rog.blade.core.Weave;
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
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Arg.class.getSimpleName()));

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
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Arg.class.getSimpleName()));

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
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Arg.class.getSimpleName()));
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
                        BundleWrapper.class,
                        Weave.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   @Weave(into=\"onAttach\", args = {\"android.content.Context\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static void inject($I target) {",
                        "       if (target.getArguments() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper args = BundleWrapper.from(target.getArguments());",
                        "       target.mExtraString = args.get(\"<Arg-mExtraString>\", target.mExtraString);",
                        "       target.mA = args.get(\"<Arg-mA>\", target.mA);",
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
