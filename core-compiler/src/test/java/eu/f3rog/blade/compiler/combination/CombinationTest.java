package eu.f3rog.blade.compiler.combination;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Arg;
import blade.Extra;
import blade.State;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.BundleWrapper;
import eu.f3rog.blade.core.Weave;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link CombinationTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class CombinationTest extends BaseTest {

    //@Test
    public void argAndState() {
        JavaFileObject input = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A",
                        State.class, "S",
                        Fragment.class
                )
                .body(
                        "public class $T extends Fragment {",
                        "",
                        "   @$A @$S int number;",
                        "",
                        "}"
                );
        JavaFileObject inputReversedOrder = file("com.example", "MainFragment")
                .imports(
                        Arg.class, "A",
                        State.class, "S",
                        Fragment.class
                )
                .body(
                        "public class $T extends Fragment {",
                        "",
                        "   @$S @$A int number;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MainFragment_Helper")
                .imports(
                        input, "I",
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E",
                        Weave.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into=\"0^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static void inject($I target) {",
                        "       if (target.getArguments() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper args = BundleWrapper.from(target.getArguments());",
                        "       target.number = args.get(\"<Arg-number>\", target.number);",
                        "   }",
                        "",
                        "   @Weave(into = \"0^onSaveInstanceState\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.saveState(this, $1);\")",
                        "   public static void saveState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-number>\", target.number);",
                        "   }",
                        "",
                        "   @Weave(into = \"1^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.restoreState(this, $1);\")",
                        "   public static void restoreState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       target.number = bundleWrapper.get(\"<Stateful-number>\", target.number);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.ARG, BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);

        assertFiles(inputReversedOrder)
                .with(BladeProcessor.Module.ARG, BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    //@Test
    public void extraAndState() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E",
                        State.class, "S",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E @$S int number;",
                        "",
                        "}"
                );
        JavaFileObject inputReversedOrder = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E",
                        State.class, "S",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$S @$E int number;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MainActivity_Helper")
                .imports(
                        input, "I",
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E",
                        Intent.class,
                        Weave.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into = \"0^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.inject(this);\")",
                        "   public static void inject($I target) {",
                        "       Intent intent = target.getIntent();",
                        "       if (intent == null || intent.getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper extras = BundleWrapper.from(intent.getExtras());",
                        "       target.number = extras.get(\"<Extra-number>\", target.number);",
                        "   }",
                        "",
                        "   @Weave(into = \"0^onSaveInstanceState\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.saveState(this, $1);\")",
                        "   public static void saveState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-number>\", target.number);",
                        "   }",
                        "",
                        "   @Weave(into = \"1^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.restoreState(this, $1);\")",
                        "   public static void restoreState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       target.number = bundleWrapper.get(\"<Stateful-number>\", target.number);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA, BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);

        assertFiles(inputReversedOrder)
                .with(BladeProcessor.Module.EXTRA, BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}
