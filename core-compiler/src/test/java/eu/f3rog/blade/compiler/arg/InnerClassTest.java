package eu.f3rog.blade.compiler.arg;

import android.app.Fragment;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Arg;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.BundleWrapper;
import eu.f3rog.blade.core.Weave;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link InnerClassTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class InnerClassTest extends BaseTest {

    @Test
    public void inner() {
        JavaFileObject input = file("com.example", "Wrapper")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        "public class $T {",
                        "",
                        "   public static class MyFragment extends Fragment {",
                        "",
                        "       @$A String mExtraString;",
                        "       @$A int mA;",
                        "   }",
                        "}"
                );

        JavaFileObject expected1 = generatedFile("com.example", "Wrapper_MyFragment_Helper")
                .imports(
                        BundleWrapper.class,
                        Weave.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into=\"0^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static void inject(Wrapper.MyFragment target) {",
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

        JavaFileObject expected2 = generatedFile("blade", "F")
                .imports(
                        input, "I",
                        BundleWrapper.class, "BW",
                        String.class
                )
                .body(
                        "public class $T {",
                        "",
                        "   public static $I.MyFragment newWrapperMyFragment(String mExtraString, int mA) {",
                        "       $I.MyFragment fragment = new $I.MyFragment();",
                        "       $BW args = new $BW();",
                        "       args.put(\"<Arg-mExtraString>\", mExtraString);",
                        "       args.put(\"<Arg-mA>\", mA);",
                        "       fragment.setArguments(args.getBundle());",
                        "       return fragment;",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2);
    }
}
