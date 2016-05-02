package eu.f3rog.blade.compiler.arg;

import android.app.Fragment;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Arg;
import blade.Blade;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link FragmentFactoryTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class FragmentFactoryTest extends BaseTest {

    public static final String COM_EXAMPLE = "com.example";
    public static final String BW_ARGS_NEW_BW = "       $BW args = new $BW();";
    public static final String BLADE = "blade";
    public static final String PUBLIC_CLASS_T = "public class $T {";
    public static final String FRAGMENT_SET_ARGUMENTS_ARGS_GET_BUNDLE = "       fragment.setArguments(args.getBundle());";
    public static final String RETURN_FRAGMENT = "       return fragment;";
    public static final String PUBLIC_CLASS_T_EXTENDS_FRAGMENT = "public class $T extends Fragment {";
    public static final String A_INT_NUMBER = "   @$A int number;";
    public static final String ARGS_PUT_ARG_NUMBER_NUMBER = "       args.put(\"<Arg-number>\", number);";
    public static final String ARGS_PUT_ARG_TEXT_TEXT = "       args.put(\"<Arg-text>\", text);";
    public static final String A_STRING_TEXT = "   @$A String text;";

    @Test
    public void none() {
        JavaFileObject input = file(COM_EXAMPLE, "SomeFragment")
                .imports(
                        Fragment.class,
                        Blade.class, "B"
                )
                .body(
                        "@$B",
                        "public class $T extends Fragment {}"
                );

        JavaFileObject expected = generatedFile(BLADE, "F")
                .imports(
                        input, "I",
                        BundleWrapper.class, "BW"
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   public static $I new$I() {",
                        "       $I fragment = new $I();",
                        BW_ARGS_NEW_BW,
                        FRAGMENT_SET_ARGUMENTS_ARGS_GET_BUNDLE,
                        RETURN_FRAGMENT,
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
    public void one() {
        JavaFileObject input = file(COM_EXAMPLE, "SomeFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_FRAGMENT,
                        "",
                        "   @$A String mText;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(BLADE, "F")
                .imports(
                        input, "I",
                        BundleWrapper.class, "BW",
                        String.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   public static $I new$I(String mText) {",
                        "       $I fragment = new $I();",
                        BW_ARGS_NEW_BW,
                        "       args.put(\"<Arg-mText>\", mText);",
                        FRAGMENT_SET_ARGUMENTS_ARGS_GET_BUNDLE,
                        RETURN_FRAGMENT,
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
    public void more() {
        JavaFileObject input1 = file(COM_EXAMPLE, "FirstFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_FRAGMENT,
                        "",
                        A_INT_NUMBER,
                        "",
                        "}"
                );
        JavaFileObject input2 = file(COM_EXAMPLE, "SecondFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_FRAGMENT,
                        "",
                        A_STRING_TEXT,
                        "   @$A boolean flag;",
                        "   @$A double number;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(BLADE, "F")
                .imports(
                        input1, "I1",
                        input2, "I2",
                        BundleWrapper.class, "BW",
                        String.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   public static $I1 new$I1(int number) {",
                        "       $I1 fragment = new $I1();",
                        BW_ARGS_NEW_BW,
                        ARGS_PUT_ARG_NUMBER_NUMBER,
                        FRAGMENT_SET_ARGUMENTS_ARGS_GET_BUNDLE,
                        RETURN_FRAGMENT,
                        "   }",
                        "",
                        "   public static $I2 new$I2(String text, boolean flag, double number) {",
                        "       $I2 fragment = new $I2();",
                        BW_ARGS_NEW_BW,
                        ARGS_PUT_ARG_TEXT_TEXT,
                        "       args.put(\"<Arg-flag>\", flag);",
                        ARGS_PUT_ARG_NUMBER_NUMBER,
                        FRAGMENT_SET_ARGUMENTS_ARGS_GET_BUNDLE,
                        RETURN_FRAGMENT,
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void inheritance() {
        JavaFileObject base = file(COM_EXAMPLE, "BaseFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_FRAGMENT,
                        "",
                        A_INT_NUMBER,
                        "",
                        "}"
                );
        JavaFileObject activity = file(COM_EXAMPLE, "MyFragment")
                .imports(
                        Arg.class, "A",
                        base, "B"
                )
                .body(
                        "public class $T extends $B {",
                        "",
                        A_STRING_TEXT,
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(BLADE, "F")
                .imports(
                        base, "B",
                        activity, "A",
                        BundleWrapper.class, "BW",
                        String.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   public static $B new$B(int number) {",
                        "       $B fragment = new $B();",
                        BW_ARGS_NEW_BW,
                        ARGS_PUT_ARG_NUMBER_NUMBER,
                        FRAGMENT_SET_ARGUMENTS_ARGS_GET_BUNDLE,
                        RETURN_FRAGMENT,
                        "   }",
                        "",
                        "   public static $A new$A(int number, String text) {",
                        "       $A fragment = new $A();",
                        BW_ARGS_NEW_BW,
                        ARGS_PUT_ARG_NUMBER_NUMBER,
                        ARGS_PUT_ARG_TEXT_TEXT,
                        FRAGMENT_SET_ARGUMENTS_ARGS_GET_BUNDLE,
                        RETURN_FRAGMENT,
                        "   }",
                        "",
                        "}"
                );

        assertFiles(base, activity)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void inheritanceFromAbstract() {
        JavaFileObject base = file(COM_EXAMPLE, "BaseFragment")
                .imports(
                        Arg.class, "A",
                        Fragment.class
                )
                .body(
                        "public abstract class $T extends Fragment {",
                        "",
                        A_INT_NUMBER,
                        "",
                        "}"
                );
        JavaFileObject activity = file(COM_EXAMPLE, "MyFragment")
                .imports(
                        Arg.class, "A",
                        base, "B"
                )
                .body(
                        "public class $T extends $B {",
                        "",
                        A_STRING_TEXT,
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(BLADE, "F")
                .imports(
                        activity, "A",
                        BundleWrapper.class, "BW",
                        String.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   public static $A new$A(int number, String text) {",
                        "       $A fragment = new $A();",
                        BW_ARGS_NEW_BW,
                        ARGS_PUT_ARG_NUMBER_NUMBER,
                        ARGS_PUT_ARG_TEXT_TEXT,
                        FRAGMENT_SET_ARGUMENTS_ARGS_GET_BUNDLE,
                        RETURN_FRAGMENT,
                        "   }",
                        "",
                        "}"
                );

        assertFiles(base, activity)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
