package eu.f3rog.blade.compiler.extra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import java.io.Serializable;

import javax.tools.JavaFileObject;

import blade.Extra;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.BundleWrapper;
import eu.f3rog.blade.core.GeneratedFor;
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
        JavaFileObject input = file("com.example", "MyActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T<T> extends Activity {",
                        "",
                        "   @$E String mData;",
                        "   @$E int mA;",
                        "}"
                );

        JavaFileObject expected1 = generatedFile("com.example", "MyActivity_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Weave.class,
                        Intent.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into = \"^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static void inject($I target) {",
                        "       Intent intent = target.getIntent();",
                        "       if (intent == null || intent.getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper extras = BundleWrapper.from(intent.getExtras());",
                        "       target.mData = extras.get(\"<Extra-mData>\", target.mData);",
                        "       target.mA = extras.get(\"<Extra-mA>\", target.mA);",
                        "   }",
                        "}"
                );

        JavaFileObject expected2 = generatedFile("blade", "I")
                .imports(
                        GeneratedFor.class, "GF",
                        input, "I",
                        BundleWrapper.class, "BW",
                        Intent.class,
                        Context.class,
                        String.class
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$GF($I.class)",
                        "   public static Intent for$I(Context context, String mData, int mA) {",
                        "       Intent intent = new Intent(context, $I.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-mData>\", mData);",
                        "       extras.put(\"<Extra-mA>\", mA);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   @$GF($I.class)",
                        "   public static void start$I(Context context, String mData, int mA) {",
                        "       context.startActivity(for$I(context, mData, mA));",
                        "   }",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2);
    }

    @Test
    public void genericField() {
        JavaFileObject input = file("com.example", "MyActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class,
                        Serializable.class
                )
                .body(
                        "public class $T<T extends Serializable> extends Activity {",
                        "",
                        "   @$E T mData;",
                        "   @$E int mA;",
                        "}"
                );

        JavaFileObject expected1 = generatedFile("com.example", "MyActivity_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Weave.class,
                        Intent.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into = \"^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static void inject($I target) {",
                        "       Intent intent = target.getIntent();",
                        "       if (intent == null || intent.getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper extras = BundleWrapper.from(intent.getExtras());",
                        "       target.mData = extras.get(\"<Extra-mData>\", target.mData);",
                        "       target.mA = extras.get(\"<Extra-mA>\", target.mA);",
                        "   }",
                        "}"
                );

        JavaFileObject expected2 = generatedFile("blade", "I")
                .imports(
                        GeneratedFor.class, "GF",
                        input, "I",
                        BundleWrapper.class, "BW",
                        Intent.class,
                        Context.class,
                        Serializable.class
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$GF($I.class)",
                        "   public static Intent for$I(Context context, Serializable mData, int mA) {",
                        "       Intent intent = new Intent(context, $I.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-mData>\", mData);",
                        "       extras.put(\"<Extra-mA>\", mA);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   @$GF($I.class)",
                        "   public static void start$I(Context context, Serializable mData, int mA) {",
                        "       context.startActivity(for$I(context, mData, mA));",
                        "   }",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2);
    }
}
