package eu.f3rog.blade.compiler.state;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.State;
import blade.mvp.IPresenter;
import blade.mvp.IView;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.core.BundleWrapper;
import eu.f3rog.blade.core.Weave;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link StateTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class StateTest extends BaseTest {

    @Test
    public void invalidField() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$S private String mText;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, State.class.getSimpleName()));

        input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$S protected String mText;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, State.class.getSimpleName()));

        input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$S final String mText;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, State.class.getSimpleName()));
    }

    @Test
    public void activity() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Activity.class,
                        State.class, "S"
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$S String mText;",
                        "   @$S int mNumber;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyClass_Helper")
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
                        "   @Weave(into = \"^onSaveInstanceState\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.saveState(this, $1);\")",
                        "   public static void saveState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-mText>\", target.mText);",
                        "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "   @Weave(into = \"^onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.restoreState(this, $1);\")",
                        "   public static void restoreState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       target.mText = bundleWrapper.get(\"<Stateful-mText>\", target.mText);",
                        "       target.mNumber = bundleWrapper.get(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void presenter() {
        JavaFileObject input = file("com.example", "MyPresenter")
                .imports(
                        IPresenter.class,
                        IView.class,
                        State.class, "S"
                )
                .body(
                        "public abstract class $T implements IPresenter<IView, Object> {",
                        "",
                        "   @$S String mText;",
                        "   @$S int mNumber;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyPresenter_Helper")
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
                        "   @Weave(into = \"^saveState\", args = {\"java.lang.Object\"}, statement = \"com.example.$T.saveState(this, (android.os.Bundle) $1);\")",
                        "   public static void saveState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-mText>\", target.mText);",
                        "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "   @Weave(into = \"^restoreState\", args = {\"java.lang.Object\"}, statement = \"com.example.$T.restoreState(this, (android.os.Bundle) $1);\")",
                        "   public static void restoreState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       target.mText = bundleWrapper.get(\"<Stateful-mText>\", target.mText);",
                        "       target.mNumber = bundleWrapper.get(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void view() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        View.class,
                        Context.class,
                        State.class, "S"
                )
                .body(
                        "public class $T extends View {",
                        "",
                        "   @$S String mText;",
                        "   @$S int mNumber;",
                        "",
                        "   public $T(Context c) {super(c);}",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyClass_Helper")
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
                        "   @Weave(into = \"^onSaveInstanceState\", ",
                        "       statement = \"android.os.Bundle bundle = new android.os.Bundle();bundle.putParcelable('PARENT_STATE', super.onSaveInstanceState());com.example.$T.saveState(this, bundle);return bundle;\")",
                        "   public static void saveState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-mText>\", target.mText);",
                        "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "   @Weave(into = \"^onRestoreInstanceState\", args = {\"android.os.Parcelable\"}, ",
                        "       statement = \"if ($1 instanceof android.os.Bundle) {android.os.Bundle bundle = (android.os.Bundle) $1;com.example.$T.restoreState(this, bundle);super.onRestoreInstanceState(bundle.getParcelable('PARENT_STATE'));} else {super.onRestoreInstanceState($1);}return;\")",
                        "   public static void restoreState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       target.mText = bundleWrapper.get(\"<Stateful-mText>\", target.mText);",
                        "       target.mNumber = bundleWrapper.get(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void other() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$S String mText;",
                        "   @$S int mNumber;",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyClass_Helper")
                .imports(
                        input, "I",
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E"
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   public static void saveState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-mText>\", target.mText);",
                        "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "   public static void restoreState($I target, Bundle state) {",
                        "       if (state == null) {",
                        "           return;",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       target.mText = bundleWrapper.get(\"<Stateful-mText>\", target.mText);",
                        "       target.mNumber = bundleWrapper.get(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
