package eu.f3rog.blade.compiler.extra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Blade;
import blade.Extra;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.BundleWrapper;
import eu.f3rog.blade.core.GeneratedFor;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link IntentManagerTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class IntentManagerTest extends BaseTest {

    public static final String COM_EXAMPLE = "com.example";
    public static final String BLADE = "blade";
    public static final String PUBLIC_CLASS_T = "public class $T {";
    public static final String GF_A_CLASS = "   @$GF($A.class)";
    public static final String INTENT_INTENT_NEW_INTENT_CONTEXT_A_CLASS = "       Intent intent = new Intent(context, $A.class);";
    public static final String BW_EXTRAS_NEW_BW = "       $BW extras = new $BW();";
    public static final String INTENT_PUT_EXTRAS_EXTRAS_GET_BUNDLE = "       intent.putExtras(extras.getBundle());";
    public static final String RETURN_INTENT = "       return intent;";
    public static final String PUBLIC_CLASS_T_EXTENDS_ACTIVITY = "public class $T extends Activity {";
    public static final String E_INT_NUMBER = "   @$E int number;";
    public static final String E_STRING_TEXT = "   @$E String text;";
    public static final String EXTRAS_PUT_EXTRA_NUMBER_NUMBER = "       extras.put(\"<Extra-number>\", number);";
    public static final String EXTRAS_PUT_EXTRA_TEXT_TEXT = "       extras.put(\"<Extra-text>\", text);";

    @Test
    public void activityNone() {
        JavaFileObject input = file(COM_EXAMPLE, "SomeActivity")
                .imports(
                        Blade.class, "B",
                        Activity.class
                )
                .body(
                        "@$B",
                        "public class $T extends Activity {}"
                );

        JavaFileObject expected = generatedFile(BLADE, "I")
                .imports(
                        GeneratedFor.class, "GF",
                        input, "A",
                        BundleWrapper.class, "BW",
                        Intent.class,
                        Context.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        GF_A_CLASS,
                        "   public static Intent for$A(Context context) {",
                        INTENT_INTENT_NEW_INTENT_CONTEXT_A_CLASS,
                        BW_EXTRAS_NEW_BW,
                        INTENT_PUT_EXTRAS_EXTRAS_GET_BUNDLE,
                        RETURN_INTENT,
                        "   }",
                        "",
                        GF_A_CLASS,
                        "   public static void start$A(Context context) {",
                        "       context.startActivity(for$A(context));",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void activityOne() {
        JavaFileObject input = file(COM_EXAMPLE, "SomeActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_ACTIVITY,
                        "",
                        "   @$E String mText;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(BLADE, "I")
                .imports(
                        GeneratedFor.class, "GF",
                        input, "A",
                        BundleWrapper.class, "BW",
                        String.class,
                        Intent.class,
                        Context.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        GF_A_CLASS,
                        "   public static Intent for$A(Context context, String mText) {",
                        INTENT_INTENT_NEW_INTENT_CONTEXT_A_CLASS,
                        BW_EXTRAS_NEW_BW,
                        "       extras.put(\"<Extra-mText>\", mText);",
                        INTENT_PUT_EXTRAS_EXTRAS_GET_BUNDLE,
                        RETURN_INTENT,
                        "   }",
                        "",
                        GF_A_CLASS,
                        "   public static void start$A(Context context, String mText) {",
                        "       context.startActivity(for$A(context, mText));",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void activityMore() {
        JavaFileObject input1 = file(COM_EXAMPLE, "FirstActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_ACTIVITY,
                        "",
                        E_INT_NUMBER,
                        "",
                        "}"
                );
        JavaFileObject input2 = file(COM_EXAMPLE, "SecondActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_ACTIVITY,
                        "",
                        E_STRING_TEXT,
                        "   @$E boolean flag;",
                        "   @$E double number;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(BLADE, "I")
                .imports(
                        GeneratedFor.class, "GF",
                        input1, "A1",
                        input2, "A2",
                        BundleWrapper.class, "BW",
                        String.class,
                        Intent.class,
                        Context.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   @$GF($A1.class)",
                        "   public static Intent for$A1(Context context, int number) {",
                        "       Intent intent = new Intent(context, $A1.class);",
                        BW_EXTRAS_NEW_BW,
                        EXTRAS_PUT_EXTRA_NUMBER_NUMBER,
                        INTENT_PUT_EXTRAS_EXTRAS_GET_BUNDLE,
                        RETURN_INTENT,
                        "   }",
                        "",
                        "   @$GF($A1.class)",
                        "   public static void start$A1(Context context, int number) {",
                        "       context.startActivity(for$A1(context, number));",
                        "   }",
                        "",
                        "   @$GF($A2.class)",
                        "   public static Intent for$A2(Context context, String text, boolean flag, double number) {",
                        "       Intent intent = new Intent(context, $A2.class);",
                        BW_EXTRAS_NEW_BW,
                        EXTRAS_PUT_EXTRA_TEXT_TEXT,
                        "       extras.put(\"<Extra-flag>\", flag);",
                        EXTRAS_PUT_EXTRA_NUMBER_NUMBER,
                        INTENT_PUT_EXTRAS_EXTRAS_GET_BUNDLE,
                        RETURN_INTENT,
                        "   }",
                        "",
                        "   @$GF($A2.class)",
                        "   public static void start$A2(Context context, String text, boolean flag, double number) {",
                        "       context.startActivity(for$A2(context, text, flag, number));",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void activityInheritance() {
        JavaFileObject base = file(COM_EXAMPLE, "BaseActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_ACTIVITY,
                        "",
                        E_INT_NUMBER,
                        "",
                        "}"
                );
        JavaFileObject activity = file(COM_EXAMPLE, "MyActivity")
                .imports(
                        Extra.class, "E",
                        base, "B"
                )
                .body(
                        "public class $T extends $B {",
                        "",
                        E_STRING_TEXT,
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(BLADE, "I")
                .imports(
                        GeneratedFor.class, "GF",
                        base, "B",
                        activity, "A",
                        BundleWrapper.class, "BW",
                        String.class,
                        Intent.class,
                        Context.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   @$GF($B.class)",
                        "   public static Intent for$B(Context context, int number) {",
                        "       Intent intent = new Intent(context, $B.class);",
                        BW_EXTRAS_NEW_BW,
                        EXTRAS_PUT_EXTRA_NUMBER_NUMBER,
                        INTENT_PUT_EXTRAS_EXTRAS_GET_BUNDLE,
                        RETURN_INTENT,
                        "   }",
                        "",
                        "   @$GF($B.class)",
                        "   public static void start$B(Context context, int number) {",
                        "       context.startActivity(for$B(context, number));",
                        "   }",
                        "",
                        GF_A_CLASS,
                        "   public static Intent for$A(Context context, int number, String text) {",
                        INTENT_INTENT_NEW_INTENT_CONTEXT_A_CLASS,
                        BW_EXTRAS_NEW_BW,
                        EXTRAS_PUT_EXTRA_NUMBER_NUMBER,
                        EXTRAS_PUT_EXTRA_TEXT_TEXT,
                        INTENT_PUT_EXTRAS_EXTRAS_GET_BUNDLE,
                        RETURN_INTENT,
                        "   }",
                        "",
                        GF_A_CLASS,
                        "   public static void start$A(Context context, int number, String text) {",
                        "       context.startActivity(for$A(context, number, text));",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(base, activity)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void activityInheritanceFromAbstract() {
        JavaFileObject base = file(COM_EXAMPLE, "BaseActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public abstract class $T extends Activity {",
                        "",
                        E_INT_NUMBER,
                        "",
                        "}"
                );
        JavaFileObject activity = file(COM_EXAMPLE, "MyActivity")
                .imports(
                        Extra.class, "E",
                        base, "B"
                )
                .body(
                        "public class $T extends $B {",
                        "",
                        E_STRING_TEXT,
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(BLADE, "I")
                .imports(
                        GeneratedFor.class, "GF",
                        activity, "A",
                        BundleWrapper.class, "BW",
                        String.class,
                        Intent.class,
                        Context.class
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        GF_A_CLASS,
                        "   public static Intent for$A(Context context, int number, String text) {",
                        INTENT_INTENT_NEW_INTENT_CONTEXT_A_CLASS,
                        BW_EXTRAS_NEW_BW,
                        EXTRAS_PUT_EXTRA_NUMBER_NUMBER,
                        EXTRAS_PUT_EXTRA_TEXT_TEXT,
                        INTENT_PUT_EXTRAS_EXTRAS_GET_BUNDLE,
                        RETURN_INTENT,
                        "   }",
                        "",
                        GF_A_CLASS,
                        "   public static void start$A(Context context, int number, String text) {",
                        "       context.startActivity(for$A(context, number, text));",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(base, activity)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
