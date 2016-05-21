package eu.f3rog.blade.compiler.arg;

import android.app.Fragment;

import org.junit.Test;

import java.io.Serializable;

import javax.tools.JavaFileObject;

import blade.Arg;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.BundleWrapper;
import eu.f3rog.blade.core.Weave;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link GenericClassTest}
 *
 * @author FrantisekGazo
 * @version 2016-04-10
 */
public final class GenericClassTest extends BaseTest {

    @Test
    public void genericClass() {
        JavaFileObject input = file("com.example", "MyFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        "public class $T<T> extends Fragment {",
                        "",
                        "   @$A String mData;",
                        "   @$A int mA;",
                        "}"
                );

        JavaFileObject expected1 = generatedFile("com.example", "MyFragment_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Weave.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into=\"^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static <T> void inject($I<T> target) {",
                        "       if (target.getArguments() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper args = BundleWrapper.from(target.getArguments());",
                        "       target.mData = args.get(\"<Arg-mData>\", target.mData);",
                        "       target.mA = args.get(\"<Arg-mA>\", target.mA);",
                        "   }",
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
                        "   public static $I new$I(String mData, int mA) {",
                        "       $I fragment = new $I();",
                        "       $BW args = new $BW();",
                        "       args.put(\"<Arg-mData>\", mData);",
                        "       args.put(\"<Arg-mA>\", mA);",
                        "       fragment.setArguments(args.getBundle());",
                        "       return fragment;",
                        "   }",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2);
    }

    @Test
    public void genericField() {
        JavaFileObject input = file("com.example", "MyFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class,
                        Serializable.class
                )
                .body(
                        "public class $T<T extends Serializable> extends Fragment implements Serializable {",
                        "",
                        "   @$A T mData;",
                        "   @$A int mA;",
                        "}"
                );

        JavaFileObject expected1 = generatedFile("com.example", "MyFragment_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Weave.class,
                        Serializable.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into=\"^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static <T extends Serializable> void inject($I<T> target) {",
                        "       if (target.getArguments() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper args = BundleWrapper.from(target.getArguments());",
                        "       target.mData = args.get(\"<Arg-mData>\", target.mData);",
                        "       target.mA = args.get(\"<Arg-mA>\", target.mA);",
                        "   }",
                        "}"
                );

        JavaFileObject expected2 = generatedFile("blade", "F")
                .imports(
                        input, "I",
                        BundleWrapper.class, "BW",
                        Serializable.class
                )
                .body(
                        "public class $T {",
                        "",
                        "   public static $I new$I(Serializable mData, int mA) {",
                        "       $I fragment = new $I();",
                        "       $BW args = new $BW();",
                        "       args.put(\"<Arg-mData>\", mData);",
                        "       args.put(\"<Arg-mA>\", mA);",
                        "       fragment.setArguments(args.getBundle());",
                        "       return fragment;",
                        "   }",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2);
    }
}
