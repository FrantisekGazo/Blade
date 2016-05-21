package eu.f3rog.blade.compiler.state;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.State;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.BundleWrapper;
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
    public void innerView() {
        JavaFileObject input = file("com.example", "A")
                .imports(
                        View.class,
                        Context.class,
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   public class B extends View {",
                        "",
                        "       @$S String mText;",
                        "       @$S int mNumber;",
                        "",
                        "       public B(Context c) {super(c);}",
                        "   }",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "A_B_Helper")
                .imports(
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
                        "   public static void saveState(A.B target, Bundle state) {",
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
                        "   public static void restoreState(A.B target, Bundle state) {",
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
    public void inner() {
        JavaFileObject input = file("com.example", "A")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   public class InnerA {",
                        "       @$S String mText;",
                        "       @$S int mNumber;",
                        "   }",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "A_InnerA_Helper")
                .imports(
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E"
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   public static void saveState(A.InnerA target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-mText>\", target.mText);",
                        "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "   public static void restoreState(A.InnerA target, Bundle state) {",
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
    public void inner2() {
        JavaFileObject input = file("com.example", "A")
                .imports(
                        State.class, "S"
                )
                .body(
                        "public class $T {",
                        "",
                        "   public class B {",
                        "",
                        "       public class C {",
                        "           @$S String mText;",
                        "           @$S int mNumber;",
                        "       }",
                        "   }",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "A_B_C_Helper")
                .imports(
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E"
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   public static void saveState(A.B.C target, Bundle state) {",
                        "       if (state == null) {",
                        "           throw new $E(\"State cannot be null!\");",
                        "       }",
                        "       BundleWrapper bundleWrapper = BundleWrapper.from(state);",
                        "       bundleWrapper.put(\"<Stateful-mText>\", target.mText);",
                        "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);",
                        "   }",
                        "",
                        "   public static void restoreState(A.B.C target, Bundle state) {",
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
