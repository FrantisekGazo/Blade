package eu.f3rog.blade.compiler.extra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.junit.Test;

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
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T {",
                        "",
                        "   public static class MyActivity extends Activity {",
                        "",
                        "       @$E String mExtraString;",
                        "       @$E int mA;",
                        "   }",
                        "}"
                );

        JavaFileObject expected1 = generatedFile("com.example", "Wrapper_MyActivity_Helper")
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
                        "   public static void inject($I.MyActivity target) {",
                        "       Intent intent = target.getIntent();",
                        "       if (intent == null || intent.getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper extras = BundleWrapper.from(intent.getExtras());",
                        "       target.mExtraString = extras.get(\"<Extra-mExtraString>\", target.mExtraString);",
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
                        "   @$GF($I.MyActivity.class)",
                        "   public static Intent for$IMyActivity(Context context, String mExtraString, int mA) {",
                        "       Intent intent = new Intent(context, $I.MyActivity.class);",
                        "       $BW extras = new $BW();",
                        "       extras.put(\"<Extra-mExtraString>\", mExtraString);",
                        "       extras.put(\"<Extra-mA>\", mA);",
                        "       intent.putExtras(extras.getBundle());",
                        "       return intent;",
                        "   }",
                        "",
                        "   @$GF($I.MyActivity.class)",
                        "   public static void start$IMyActivity(Context context, String mExtraString, int mA) {",
                        "       context.startActivity(for$IMyActivity(context, mExtraString, mA));",
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
