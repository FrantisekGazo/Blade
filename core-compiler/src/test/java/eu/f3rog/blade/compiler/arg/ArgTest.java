package eu.f3rog.blade.compiler.arg;

import android.app.Fragment;

import org.junit.Test;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import blade.Arg;
import blade.Blade;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.core.BundleWrapper;
import eu.f3rog.blade.core.Weave;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link ArgTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class ArgTest extends BaseTest {

    public static final String MAIN_FRAGMENT = "MainFragment";
    public static final String COM_EXAMPLE = "com.example";
    public static final String PUBLIC_CLASS_T_EXTENDS_FRAGMENT = "public class $T extends Fragment {";

    @Test
    public void invalidCLass() {
        JavaFileObject input = file(COM_EXAMPLE, MAIN_FRAGMENT)
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
                .with(BladeProcessor.Module.ARG)
                .failsToCompile()
                .withErrorContaining(ArgErrorMsg.Invalid_class_with_Arg);
    }

    @Test
    public void invalidField() {
        JavaFileObject input = file(COM_EXAMPLE, MAIN_FRAGMENT)
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_FRAGMENT,
                        "",
                        "   @$A private String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Arg.class.getSimpleName()));

        input = file(COM_EXAMPLE, MAIN_FRAGMENT)
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_FRAGMENT,
                        "",
                        "   @$A protected String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Arg.class.getSimpleName()));

        input = file(COM_EXAMPLE, MAIN_FRAGMENT)
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_FRAGMENT,
                        "",
                        "   @$A final String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Arg.class.getSimpleName()));
    }

    @Test
    public void one() {
        JavaFileObject input = file(COM_EXAMPLE, MAIN_FRAGMENT)
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_FRAGMENT,
                        "",
                        "   @$A String mExtraString;",
                        "   @$A int mA;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "MainFragment_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Weave.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into=\"onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
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
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void none() {
        JavaFileObject input = file(COM_EXAMPLE, MAIN_FRAGMENT)
                .imports(
                        Blade.class, "B",
                        Fragment.class
                )
                .body(
                        "@$B",
                        "public class $T extends Fragment {}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesFileNamed(StandardLocation.CLASS_OUTPUT, "blade", "F.class");
    }

}
