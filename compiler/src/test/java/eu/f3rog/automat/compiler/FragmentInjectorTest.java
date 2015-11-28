package eu.f3rog.automat.compiler;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import org.junit.Test;

import javax.tools.JavaFileObject;

import eu.f3rog.automat.Arg;
import eu.f3rog.automat.Extra;

import static eu.f3rog.automat.compiler.util.File.file;
import static eu.f3rog.automat.compiler.util.File.generatedFile;

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
                        Arg.class, "A"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$A private String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Arg.toString());

        input = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$A protected String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Arg.toString());

            input = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$A final String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Arg.toString());
    }

    @Test
    public void test1() {
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

        JavaFileObject expected = generatedFile("com.example", "MainFragment_Injector")
                .imports(
                        input, "I",
                        Bundle.class,
                        String.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public void inject($I target) {",
                        "       if (target.getArguments() == null) {",
                        "           return;",
                        "       }",
                        "       Bundle args = target.getArguments();",
                        "       target.mExtraString = (String) args.getString(\"com.example.$I-mExtraString\");",
                        "       target.mA = (int) args.getInt(\"com.example.$I-mA\");",
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
