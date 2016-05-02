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

    public static final String MY_CLASS = "MyClass";
    public static final String COM_EXAMPLE = "com.example";
    public static final String PUBLIC_CLASS_T = "public class $T {";
    public static final String S_STRING_M_TEXT = "   @$S String mText;";
    public static final String S_INT_M_NUMBER = "   @$S int mNumber;";
    public static final String ABSTRACT_CLASS_T = "abstract class $T {";
    public static final String PUBLIC_STATIC_VOID_SAVE_STATE_I_TARGET_BUNDLE_STATE = "   public static void saveState($I target, Bundle state) {";
    public static final String IF_STATE_NULL = "       if (state == null) {";
    public static final String THROW_NEW_E_STATE_CANNOT_BE_NULL = "           throw new $E(\"State cannot be null!\");";
    public static final String CLOSING_BRACE = "       }";
    public static final String BUNDLE_WRAPPER_BUNDLE_WRAPPER_BUNDLE_WRAPPER_FROM_STATE = "       BundleWrapper bundleWrapper = BundleWrapper.from(state);";
    public static final String BUNDLE_WRAPPER_PUT_STATEFUL_M_TEXT_TARGET_M_TEXT = "       bundleWrapper.put(\"<Stateful-mText>\", target.mText);";
    public static final String BUNDLE_WRAPPER_PUT_STATEFUL_M_NUMBER_TARGET_M_NUMBER = "       bundleWrapper.put(\"<Stateful-mNumber>\", target.mNumber);";
    public static final String PUBLIC_STATIC_VOID_RESTORE_STATE_I_TARGET_BUNDLE_STATE = "   public static void restoreState($I target, Bundle state) {";
    public static final String RETURN = "           return;";
    public static final String TARGET_M_TEXT_BUNDLE_WRAPPER_GET_STATEFUL_M_TEXT_TARGET_M_TEXT = "       target.mText = bundleWrapper.get(\"<Stateful-mText>\", target.mText);";
    public static final String TARGET_M_NUMBER_BUNDLE_WRAPPER_GET_STATEFUL_M_NUMBER_TARGET_M_NUMBER = "       target.mNumber = bundleWrapper.get(\"<Stateful-mNumber>\", target.mNumber);";

    @Test
    public void invalidField() {
        JavaFileObject input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        State.class, "S"
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   @$S private String mText;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, State.class.getSimpleName()));

        input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        State.class, "S"
                )
                .body(
                        PUBLIC_CLASS_T,
                        "",
                        "   @$S protected String mText;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, State.class.getSimpleName()));

        input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        State.class, "S"
                )
                .body(
                        PUBLIC_CLASS_T,
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
        JavaFileObject input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        Activity.class,
                        State.class, "S"
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        S_STRING_M_TEXT,
                        S_INT_M_NUMBER,
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "MyClass_Helper")
                .imports(
                        input, "I",
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E",
                        Weave.class
                )
                .body(
                        ABSTRACT_CLASS_T,
                        "",
                        "   @Weave(into = \"onSaveInstanceState\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.saveState(this, $1);\")",
                        PUBLIC_STATIC_VOID_SAVE_STATE_I_TARGET_BUNDLE_STATE,
                        IF_STATE_NULL,
                        THROW_NEW_E_STATE_CANNOT_BE_NULL,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_BUNDLE_WRAPPER_BUNDLE_WRAPPER_FROM_STATE,
                        BUNDLE_WRAPPER_PUT_STATEFUL_M_TEXT_TARGET_M_TEXT,
                        BUNDLE_WRAPPER_PUT_STATEFUL_M_NUMBER_TARGET_M_NUMBER,
                        "   }",
                        "",
                        "   @Weave(into = \"onCreate\", args = {\"android.os.Bundle\"}, statement = \"com.example.$T.restoreState(this, $1);\")",
                        PUBLIC_STATIC_VOID_RESTORE_STATE_I_TARGET_BUNDLE_STATE,
                        IF_STATE_NULL,
                        RETURN,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_BUNDLE_WRAPPER_BUNDLE_WRAPPER_FROM_STATE,
                        TARGET_M_TEXT_BUNDLE_WRAPPER_GET_STATEFUL_M_TEXT_TARGET_M_TEXT,
                        TARGET_M_NUMBER_BUNDLE_WRAPPER_GET_STATEFUL_M_NUMBER_TARGET_M_NUMBER,
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
        JavaFileObject input = file(COM_EXAMPLE, "MyPresenter")
                .imports(
                        IPresenter.class,
                        IView.class,
                        State.class, "S"
                )
                .body(
                        "public abstract class $T implements IPresenter<IView, Object> {",
                        "",
                        S_STRING_M_TEXT,
                        S_INT_M_NUMBER,
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "MyPresenter_Helper")
                .imports(
                        input, "I",
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E",
                        Weave.class
                )
                .body(
                        ABSTRACT_CLASS_T,
                        "",
                        "   @Weave(into = \"saveState\", args = {\"java.lang.Object\"}, statement = \"com.example.$T.saveState(this, (android.os.Bundle) $1);\")",
                        PUBLIC_STATIC_VOID_SAVE_STATE_I_TARGET_BUNDLE_STATE,
                        IF_STATE_NULL,
                        THROW_NEW_E_STATE_CANNOT_BE_NULL,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_BUNDLE_WRAPPER_BUNDLE_WRAPPER_FROM_STATE,
                        BUNDLE_WRAPPER_PUT_STATEFUL_M_TEXT_TARGET_M_TEXT,
                        BUNDLE_WRAPPER_PUT_STATEFUL_M_NUMBER_TARGET_M_NUMBER,
                        "   }",
                        "",
                        "   @Weave(into = \"restoreState\", args = {\"java.lang.Object\"}, statement = \"com.example.$T.restoreState(this, (android.os.Bundle) $1);\")",
                        PUBLIC_STATIC_VOID_RESTORE_STATE_I_TARGET_BUNDLE_STATE,
                        IF_STATE_NULL,
                        RETURN,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_BUNDLE_WRAPPER_BUNDLE_WRAPPER_FROM_STATE,
                        TARGET_M_TEXT_BUNDLE_WRAPPER_GET_STATEFUL_M_TEXT_TARGET_M_TEXT,
                        TARGET_M_NUMBER_BUNDLE_WRAPPER_GET_STATEFUL_M_NUMBER_TARGET_M_NUMBER,
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
        JavaFileObject input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        View.class,
                        Context.class,
                        State.class, "S"
                )
                .body(
                        "public class $T extends View {",
                        "",
                        S_STRING_M_TEXT,
                        S_INT_M_NUMBER,
                        "",
                        "   public $T(Context c) {super(c);}",
                        "}"
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "MyClass_Helper")
                .imports(
                        input, "I",
                        Bundle.class,
                        BundleWrapper.class,
                        IllegalArgumentException.class, "E",
                        Weave.class
                )
                .body(
                        ABSTRACT_CLASS_T,
                        "",
                        "   @Weave(into = \"onSaveInstanceState\", ",
                        "       statement = \"android.os.Bundle bundle = new android.os.Bundle();bundle.putParcelable('PARENT_STATE', super.onSaveInstanceState());com.example.$T.saveState(this, bundle);return bundle;\")",
                        PUBLIC_STATIC_VOID_SAVE_STATE_I_TARGET_BUNDLE_STATE,
                        IF_STATE_NULL,
                        THROW_NEW_E_STATE_CANNOT_BE_NULL,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_BUNDLE_WRAPPER_BUNDLE_WRAPPER_FROM_STATE,
                        BUNDLE_WRAPPER_PUT_STATEFUL_M_TEXT_TARGET_M_TEXT,
                        BUNDLE_WRAPPER_PUT_STATEFUL_M_NUMBER_TARGET_M_NUMBER,
                        "   }",
                        "",
                        "   @Weave(into = \"onRestoreInstanceState\", args = {\"android.os.Parcelable\"}, ",
                        "       statement = \"if ($1 instanceof android.os.Bundle) {android.os.Bundle bundle = (android.os.Bundle) $1;com.example.$T.restoreState(this, bundle);super.onRestoreInstanceState(bundle.getParcelable('PARENT_STATE'));} else {super.onRestoreInstanceState($1);}return;\")",
                        PUBLIC_STATIC_VOID_RESTORE_STATE_I_TARGET_BUNDLE_STATE,
                        IF_STATE_NULL,
                        RETURN,
                        CLOSING_BRACE,
                        BUNDLE_WRAPPER_BUNDLE_WRAPPER_BUNDLE_WRAPPER_FROM_STATE,
                        TARGET_M_TEXT_BUNDLE_WRAPPER_GET_STATEFUL_M_TEXT_TARGET_M_TEXT,
                        TARGET_M_NUMBER_BUNDLE_WRAPPER_GET_STATEFUL_M_NUMBER_TARGET_M_NUMBER,
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
