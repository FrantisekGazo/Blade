package eu.f3rog.blade.compiler;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Extra;
import eu.f3rog.blade.core.BundleWrapper;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link IntentManagerTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class IntentManagerTest extends BaseTest {

    @Test
    public void one() {
        JavaFileObject input = file("com.example", "SomeActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E String mText;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("blade", "I")
                .imports(
                        input, "A",
                        BundleWrapper.class, "BW",
                        String.class,
                        Intent.class,
                        Context.class,
                        Activity.class,
                        Fragment.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static Intent for$A(Context context, String mText) {",
                        "       Intent intent = new Intent(context, $A.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-mText>\", mText);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   public static void start$A(Context context, String mText) {",
                        "       context.startActivity(for$A(context, mText));",
                        "   }",
                        "",
                        "   public static void start$AForResult(Activity activity, int requestCode, String mText) {",
                        "       activity.startActivityForResult(for$A(activity, mText), requestCode);",
                        "   }",
                        "",
                        "   public static void start$AForResult(Fragment fragment, int requestCode, String mText) {",
                        "       fragment.startActivityForResult(for$A(fragment.getActivity(), mText), requestCode);",
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
    public void more() {
        JavaFileObject input1 = file("com.example", "FirstActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E int number;",
                        "",
                        "}"
                );
        JavaFileObject input2 = file("com.example", "SecondActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E String text;",
                        "   @$E boolean flag;",
                        "   @$E double number;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("blade", "I")
                .imports(
                        input1, "A1",
                        input2, "A2",
                        BundleWrapper.class, "BW",
                        String.class,
                        Intent.class,
                        Context.class,
                        Activity.class,
                        Fragment.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static Intent for$A1(Context context, int number) {",
                        "       Intent intent = new Intent(context, $A1.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-number>\", number);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   public static void start$A1(Context context, int number) {",
                        "       context.startActivity(for$A1(context, number));",
                        "   }",
                        "",
                        "   public static void start$A1ForResult(Activity activity, int requestCode, int number) {",
                        "       activity.startActivityForResult(for$A1(activity, number), requestCode);",
                        "   }",
                        "",
                        "   public static void start$A1ForResult(Fragment fragment, int requestCode, int number) {",
                        "       fragment.startActivityForResult(for$A1(fragment.getActivity(), number), requestCode);",
                        "   }",
                        "",
                        "   public static Intent for$A2(Context context, String text, boolean flag, double number) {",
                        "       Intent intent = new Intent(context, $A2.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-text>\", text);",
                        "       extras.put(\"<Extra-flag>\", flag);",
                        "       extras.put(\"<Extra-number>\", number);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   public static void start$A2(Context context, String text, boolean flag, double number) {",
                        "       context.startActivity(for$A2(context, text, flag, number));",
                        "   }",
                        "",
                        "   public static void start$A2ForResult(Activity activity, int requestCode, String text, boolean flag, double number) {",
                        "       activity.startActivityForResult(for$A2(activity, text, flag, number), requestCode);",
                        "   }",
                        "",
                        "   public static void start$A2ForResult(Fragment fragment, int requestCode, String text, boolean flag, double number) {",
                        "       fragment.startActivityForResult(for$A2(fragment.getActivity(), text, flag, number), requestCode);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input1, input2)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void inherit() {
        JavaFileObject base = file("com.example", "BaseActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E int number;",
                        "",
                        "}"
                );
        JavaFileObject activity = file("com.example", "MyActivity")
                .imports(
                        Extra.class, "E",
                        base, "B"
                )
                .body(
                        "public class $T extends $B {",
                        "",
                        "   @$E String text;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("blade", "I")
                .imports(
                        base, "B",
                        activity, "A",
                        BundleWrapper.class, "BW",
                        String.class,
                        Intent.class,
                        Context.class,
                        Activity.class,
                        Fragment.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static Intent for$B(Context context, int number) {",
                        "       Intent intent = new Intent(context, $B.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-number>\", number);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   public static void start$B(Context context, int number) {",
                        "       context.startActivity(for$B(context, number));",
                        "   }",
                        "",
                        "   public static void start$BForResult(Activity activity, int requestCode, int number) {",
                        "       activity.startActivityForResult(for$B(activity, number), requestCode);",
                        "   }",
                        "",
                        "   public static void start$BForResult(Fragment fragment, int requestCode, int number) {",
                        "       fragment.startActivityForResult(for$B(fragment.getActivity(), number), requestCode);",
                        "   }",
                        "",
                        "   public static Intent for$A(Context context, int number, String text) {",
                        "       Intent intent = new Intent(context, $A.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-number>\", number);",
                        "       extras.put(\"<Extra-text>\", text);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   public static void start$A(Context context, int number, String text) {",
                        "       context.startActivity(for$A(context, number, text));",
                        "   }",
                        "",
                        "   public static void start$AForResult(Activity activity, int requestCode, int number, String text) {",
                        "       activity.startActivityForResult(for$A(activity, number, text), requestCode);",
                        "   }",
                        "",
                        "   public static void start$AForResult(Fragment fragment, int requestCode, int number, String text) {",
                        "       fragment.startActivityForResult(for$A(fragment.getActivity(), number, text), requestCode);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(base, activity)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void inheritanceFromAbstract() {
        JavaFileObject base = file("com.example", "BaseActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public abstract class $T extends Activity {",
                        "",
                        "   @$E int number;",
                        "",
                        "}"
                );
        JavaFileObject activity = file("com.example", "MyActivity")
                .imports(
                        Extra.class, "E",
                        base, "B"
                )
                .body(
                        "public class $T extends $B {",
                        "",
                        "   @$E String text;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("blade", "I")
                .imports(
                        activity, "A",
                        BundleWrapper.class, "BW",
                        String.class,
                        Intent.class,
                        Context.class,
                        Activity.class,
                        Fragment.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public static Intent for$A(Context context, int number, String text) {",
                        "       Intent intent = new Intent(context, $A.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-number>\", number);",
                        "       extras.put(\"<Extra-text>\", text);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   public static void start$A(Context context, int number, String text) {",
                        "       context.startActivity(for$A(context, number, text));",
                        "   }",
                        "",
                        "   public static void start$AForResult(Activity activity, int requestCode, int number, String text) {",
                        "       activity.startActivityForResult(for$A(activity, number, text), requestCode);",
                        "   }",
                        "",
                        "   public static void start$AForResult(Fragment fragment, int requestCode, int number, String text) {",
                        "       fragment.startActivityForResult(for$A(fragment.getActivity(), number, text), requestCode);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(base, activity)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
