package eu.f3rog.blade.compiler.extra;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.junit.Test;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import blade.Blade;
import blade.Extra;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.compiler.ErrorMsg;
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

    public static final String MAIN_ACTIVITY = "MainActivity";
    public static final String COM_EXAMPLE = "com.example";
    public static final String E_STRING_M_EXTRA_STRING = "   @$E String mExtraString;";
    public static final String PUBLIC_CLASS_T_EXTENDS_ACTIVITY = "public class $T extends Activity {";
    public static final String E_INT_M_A = "   @$E int mA;";
    public static final String ABSTRACT_CLASS_T = "abstract class $T {";
    public static final String IF_INTENT_NULL_INTENT_GET_EXTRAS_NULL = "       if (intent == null || intent.getExtras() == null) {";
    public static final String RETURN = "           return;";
    public static final String BUNDLE_WRAPPER_EXTRAS_BUNDLE_WRAPPER_FROM_INTENT_GET_EXTRAS = "       BundleWrapper extras = BundleWrapper.from(intent.getExtras());";
    public static final String TARGET_M_EXTRA_STRING_EXTRAS_GET_EXTRA_M_EXTRA_STRING_TARGET_M_EXTRA_STRING = "       target.mExtraString = extras.get(\"<Extra-mExtraString>\", target.mExtraString);";
    public static final String TARGET_M_A_EXTRAS_GET_EXTRA_M_A_TARGET_M_A = "       target.mA = extras.get(\"<Extra-mA>\", target.mA);";
    public static final String CLOSING_BRACE = "       }";

    @Test
    public void invalidCLass() {
        JavaFileObject input = file(COM_EXAMPLE, MAIN_ACTIVITY)
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        E_STRING_M_EXTRA_STRING,
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .failsToCompile()
                .withErrorContaining(ExtraErrorMsg.Invalid_class_with_Extra);
    }

    @Test
    public void invalidField() {
        JavaFileObject input = file(COM_EXAMPLE, MAIN_ACTIVITY)
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_ACTIVITY,
                        "",
                        "   @$E private String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Extra.class.getSimpleName()));

        input = file(COM_EXAMPLE, MAIN_ACTIVITY)
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_ACTIVITY,
                        "",
                        "   @$E protected String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Extra.class.getSimpleName()));

        input = file(COM_EXAMPLE, MAIN_ACTIVITY)
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_ACTIVITY,
                        "",
                        "   @$E final String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Extra.class.getSimpleName()));
    }

    @Test
    public void activityNone() {
        JavaFileObject input = file(COM_EXAMPLE, MAIN_ACTIVITY)
                .imports(
                        Blade.class, "B",
                        Activity.class
                )
                .body(
                        "@$B",
                        "public class $T extends Activity {}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesFileNamed(StandardLocation.CLASS_OUTPUT, "blade", "I.class");
    }

    @Test
    public void activityOne() {
        JavaFileObject input = file(COM_EXAMPLE, MAIN_ACTIVITY)
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_ACTIVITY,
                        "",
                        E_STRING_M_EXTRA_STRING,
                        E_INT_M_A,
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "MainActivity_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Weave.class,
                        Intent.class
                )
                .body(
                        ABSTRACT_CLASS_T,
                        "",
                        "   @Weave(into = \"onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static void inject($I target) {",
                        "       Intent intent = target.getIntent();",
                        IF_INTENT_NULL_INTENT_GET_EXTRAS_NULL,
                        RETURN,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_EXTRAS_BUNDLE_WRAPPER_FROM_INTENT_GET_EXTRAS,
                        TARGET_M_EXTRA_STRING_EXTRAS_GET_EXTRA_M_EXTRA_STRING_TARGET_M_EXTRA_STRING,
                        TARGET_M_A_EXTRAS_GET_EXTRA_M_A_TARGET_M_A,
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
    public void serviceOne() {
        JavaFileObject input = file(COM_EXAMPLE, "SomeService")
                .imports(
                        Extra.class, "E",
                        Service.class,
                        Intent.class,
                        IBinder.class
                )
                .body(
                        "public class $T extends Service {",
                        "",
                        E_STRING_M_EXTRA_STRING,
                        E_INT_M_A,
                        "",
                        "   public IBinder onBind(Intent intent) {",
                        "       return null;",
                        "   }",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "SomeService_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Intent.class,
                        Weave.class
                )
                .body(
                        ABSTRACT_CLASS_T,
                        "",
                        "   @Weave(into = \"onStartCommand\", args = {\"android.content.Intent\", \"int\", \"int\"}, statement = \"com.example.$T.inject(this, $1);\")",
                        "   public static void inject($I target, Intent intent) {",
                        IF_INTENT_NULL_INTENT_GET_EXTRAS_NULL,
                        RETURN,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_EXTRAS_BUNDLE_WRAPPER_FROM_INTENT_GET_EXTRAS,
                        TARGET_M_EXTRA_STRING_EXTRAS_GET_EXTRA_M_EXTRA_STRING_TARGET_M_EXTRA_STRING,
                        TARGET_M_A_EXTRAS_GET_EXTRA_M_A_TARGET_M_A,
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
    public void intentServiceOne() {
        JavaFileObject input = file(COM_EXAMPLE, "SomeService")
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
                        E_INT_M_A,
                        "",
                        "   public $T() {super(\"Test\");}",
                        "",
                        "   @Override",
                        "   protected void onHandleIntent(Intent intent) {}",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "SomeService_Helper")
                .imports(
                        input, "I",
                        BundleWrapper.class,
                        Intent.class,
                        Weave.class
                )
                .body(
                        ABSTRACT_CLASS_T,
                        "",
                        "   @Weave(into = \"onHandleIntent\", args = {\"android.content.Intent\"}, statement = \"com.example.$T.inject(this, $1);\")",
                        "   public static void inject($I target, Intent intent) {",
                        IF_INTENT_NULL_INTENT_GET_EXTRAS_NULL,
                        RETURN,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_EXTRAS_BUNDLE_WRAPPER_FROM_INTENT_GET_EXTRAS,
                        TARGET_M_EXTRA_STRING_EXTRAS_GET_EXTRA_M_EXTRA_STRING_TARGET_M_EXTRA_STRING,
                        TARGET_M_A_EXTRAS_GET_EXTRA_M_A_TARGET_M_A,
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
}
