package eu.f3rog.blade.compiler;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Extra;
import eu.f3rog.blade.core.BundleWrapper;
import eu.f3rog.blade.core.Weave;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link ExtraTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class ExtraTest extends BaseTest {

    @Test
    public void invalidCLass() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_class_with_Extra.toString());
    }

    @Test
    public void invalidField() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E private String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Extra.class.getSimpleName()));

        input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E protected String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Extra.class.getSimpleName()));

        input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E final String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Extra.class.getSimpleName()));
    }

    @Test
    public void oneActivity() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E String mExtraString;",
                        "   @$E int mA;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MainActivity_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Weave.class,
                        Intent.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   @Weave(into = \"onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static void inject($I target) {",
                        "       Intent intent = target.getIntent();",
                        "       if (intent == null || intent.getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper extras = BundleWrapper.from(intent.getExtras());",
                        "       target.mExtraString = extras.get(\"<Extra-mExtraString>\", target.mExtraString);",
                        "       target.mA = extras.get(\"<Extra-mA>\", target.mA);",
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
    public void oneService() {
        JavaFileObject input = file("com.example", "SomeService")
                .imports(
                        Extra.class, "E",
                        Service.class,
                        Intent.class,
                        IBinder.class
                )
                .body(
                        "public class $T extends Service {",
                        "",
                        "   @$E String mExtraString;",
                        "   @$E int mA;",
                        "",
                        "   public IBinder onBind(Intent intent) {",
                        "       return null;",
                        "   }",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "SomeService_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Intent.class,
                        Weave.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   @Weave(into = \"onStartCommand\", args = {\"android.content.Intent\", \"int\", \"int\"}, statement = \"com.example.$T.inject(this, $1);\")",
                        "   public static void inject($I target, Intent intent) {",
                        "       if (intent == null || intent.getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper extras = BundleWrapper.from(intent.getExtras());",
                        "       target.mExtraString = extras.get(\"<Extra-mExtraString>\", target.mExtraString);",
                        "       target.mA = extras.get(\"<Extra-mA>\", target.mA);",
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
    public void oneIntentService() {
        JavaFileObject input = file("com.example", "SomeService")
                .imports(
                        Extra.class, "E",
                        IntentService.class,
                        Intent.class,
                        IBinder.class
                )
                .body(
                        "public class $T extends IntentService {",
                        "",
                        "   @$E String mExtraString;",
                        "   @$E int mA;",
                        "",
                        "   public $T() {super(\"Test\");}",
                        "",
                        "   @Override",
                        "   protected void onHandleIntent(Intent intent) {}",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "SomeService_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Intent.class,
                        Weave.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   @Weave(into = \"onHandleIntent\", args = {\"android.content.Intent\"}, statement = \"com.example.$T.inject(this, $1);\")",
                        "   public static void inject($I target, Intent intent) {",
                        "       if (intent == null || intent.getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper extras = BundleWrapper.from(intent.getExtras());",
                        "       target.mExtraString = extras.get(\"<Extra-mExtraString>\", target.mExtraString);",
                        "       target.mA = extras.get(\"<Extra-mA>\", target.mA);",
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
