package eu.f3rog.blade.compiler.mvp;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Presenter;
import blade.mvp.BasePresenter;
import blade.mvp.IPresenter;
import blade.mvp.IView;
import blade.mvp.PresenterManager;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.core.Weave;
import eu.f3rog.blade.mvp.MvpActivity;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link PresenterTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class PresenterTest extends BaseTest {

    private static final String PRESENTER_METHODS =
        " public void bind(%s view) {} " +
                " public void unbind() {} " +
                " public void create(%s o, boolean wasRestored) {} " +
                " public void destroy() {} " +
                " public void saveState(Object o) {} " +
                " public void restoreState(Object o) {} ";
    public static final String MY_CLASS = "MyClass";
    public static final String COM_EXAMPLE = "com.example";
    public static final String P_OBJECT_O = "   @$P Object o;";
    public static final String PUBLIC_CLASS_T_EXTENDS_VIEW_IMPLEMENTS_V = "public class $T extends View implements $V {";
    public static final String PUBLIC_T_CONTEXT_C_SUPER_C = "   public $T(Context c) {super(c);}";
    public static final String STRING = "String";
    public static final String MY_PRESENTER = "MyPresenter";
    public static final String P_MP_M_PRESENTER = "   @$P $MP mPresenter;";
    public static final String IF_TAG_OBJECT_NULL = "       if (tagObject == null) {";
    public static final String IF_TARGET_M_PRESENTER_NULL_1 = "           if (target.mPresenter != null) {";
    public static final String TARGET_M_PRESENTER_UNBIND = "               target.mPresenter.unbind();";
    public static final String TARGET_M_PRESENTER_NULL = "           target.mPresenter = null;";
    public static final String ELSE = "       } else {";
    public static final String IF_TAG_OBJECT_INSTANCEOF_STRING = "           if (!(tagObject instanceof String)) {";
    public static final String THROW_NEW_E_INCORRECT_TYPE_OF_TAG_OBJECT = "               throw new $E(\"Incorrect type of tag object.\");";
    public static final String STRING_PARAM_STRING_TAG_OBJECT = "           String param = (String) tagObject;";
    public static final String TARGET_M_PRESENTER_P_PM_GET_TARGET_PARAM_P_CLASS = "           target.mPresenter = ($P) $PM.get(target, param, $P.class);";
    public static final String IF_TARGET_M_PRESENTER_NULL_2 = "           if (target.mPresenter == null) {";
    public static final String TARGET_M_PRESENTER_NEW_P = "               target.mPresenter = new $P();";
    public static final String PM_PUT_TARGET_PARAM_TARGET_M_PRESENTER = "               $PM.put(target, param, target.mPresenter);";
    public static final String CLOSING_BRACE_1 = "       }";
    public static final String IF_TARGET_M_PRESENTER_NULL_3 = "       if (target.mPresenter != null) {";
    public static final String TARGET_M_PRESENTER_BIND_TARGET = "           target.mPresenter.bind(target);";
    public static final String CLOSING_BRACE_2 = "   }";
    public static final String CLOSING_BRACE_3 = "           }";
    public static final String CLOSING_BRACE_4 = "}";

    @Test
    public void invalidClass() {
        JavaFileObject input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        Presenter.class, "P"
                )
                .body(
                        "public class $T {",
                        "",
                        P_OBJECT_O,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_Presenter);

        input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        Presenter.class, "P",
                        View.class
                )
                .body(
                        "public class $T extends View {",
                        "",
                        P_OBJECT_O,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_Presenter);
    }

    @Test
    public void invalidField() {
        JavaFileObject input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_VIEW_IMPLEMENTS_V,
                        "",
                        "   @$P private Object o;",
                        "",
                        PUBLIC_T_CONTEXT_C_SUPER_C,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Presenter.class.getSimpleName()));

        input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_VIEW_IMPLEMENTS_V,
                        "",
                        "   @$P protected Object o;",
                        "",
                        PUBLIC_T_CONTEXT_C_SUPER_C,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Presenter.class.getSimpleName()));

        input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_VIEW_IMPLEMENTS_V,
                        "",
                        "   @$P final Object o;",
                        "",
                        PUBLIC_T_CONTEXT_C_SUPER_C,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Presenter.class.getSimpleName()));
    }

    @Test
    public void invalidPresenterClass() {
        JavaFileObject input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_VIEW_IMPLEMENTS_V,
                        "",
                        P_OBJECT_O,
                        "",
                        PUBLIC_T_CONTEXT_C_SUPER_C,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_Presenter_class);
    }

    @Test
    public void inconsistentPresenterParameterTypes() {
        JavaFileObject presenter1 = file(COM_EXAMPLE, "MyPresenter1")
                .imports(
                        IPresenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T implements $P<$V, Long> {",
                        "",
                        String.format(PRESENTER_METHODS, "$V", "Long"),
                        "",
                        CLOSING_BRACE_4
                );
        JavaFileObject presenter2 = file(COM_EXAMPLE, "MyPresenter2")
                .imports(
                        IPresenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T implements $P<$V, String> {",
                        "",
                        String.format(PRESENTER_METHODS, "$V", STRING),
                        "",
                        CLOSING_BRACE_4
                );
        JavaFileObject input = file(COM_EXAMPLE, MY_CLASS)
                .imports(
                        presenter1, "MP1",
                        presenter2, "MP2",
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_VIEW_IMPLEMENTS_V,
                        "",
                        "   @$P $MP1 mPresenter1;",
                        "   @$P $MP2 mPresenter2;",
                        "",
                        PUBLIC_T_CONTEXT_C_SUPER_C,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(presenter1, presenter2, input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Inconsistent_Presenter_parameter_classes);
    }

    @Test
    public void oneViewPresenter() {
        JavaFileObject presenter = file(COM_EXAMPLE, MY_PRESENTER)
                .imports(
                        IPresenter.class, "P",
                        "com.example.MyView", "V"
                )
                .body(
                        "public class $T implements $P<$V, String> {",
                        "",
                        String.format(PRESENTER_METHODS, "$V", STRING),
                        "",
                        CLOSING_BRACE_4
                );
        JavaFileObject view = file(COM_EXAMPLE, "MyView")
                .imports(
                        presenter, "MP",
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_VIEW_IMPLEMENTS_V,
                        "",
                        P_MP_M_PRESENTER,
                        "",
                        PUBLIC_T_CONTEXT_C_SUPER_C,
                        "",
                        CLOSING_BRACE_4
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "MyView_Helper")
                .imports(
                        PresenterManager.class, "PM",
                        presenter, "P",
                        view, "V",
                        Object.class,
                        String.class,
                        Weave.class,
                        IllegalStateException.class, "E"
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into = \"<FIELD>\", statement = \"\")",
                        "   private boolean mIsAttached;",
                        "",
                        "   @Weave(into = \"setTag\", args = {\"java.lang.Object\"}, statement = \"String tag = com.example.$T.setPresenters(this, $1); super.setTag(tag); if (this.mIsAttached) { com.example.$T.bindPresenters(this); } return;\")",
                        "   public static String setPresenters($V target, Object tagObject) {",
                        IF_TAG_OBJECT_NULL,
                        IF_TARGET_M_PRESENTER_NULL_1,
                        TARGET_M_PRESENTER_UNBIND,
                        CLOSING_BRACE_1,
                        TARGET_M_PRESENTER_NULL,
                        "           return null;",
                        ELSE,
                        IF_TAG_OBJECT_INSTANCEOF_STRING,
                        THROW_NEW_E_INCORRECT_TYPE_OF_TAG_OBJECT,
                        CLOSING_BRACE_3,
                        STRING_PARAM_STRING_TAG_OBJECT,
                        TARGET_M_PRESENTER_P_PM_GET_TARGET_PARAM_P_CLASS,
                        IF_TARGET_M_PRESENTER_NULL_2,
                        TARGET_M_PRESENTER_NEW_P,
                        PM_PUT_TARGET_PARAM_TARGET_M_PRESENTER,
                        CLOSING_BRACE_3,
                        "           return tagObject.toString();",
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        "   @Weave(into = \"onAttachedToWindow\", statement = \"com.example.$T.bindPresenters(this); this.mIsAttached = true;\")",
                        "   public static void bindPresenters($V target) {",
                        IF_TARGET_M_PRESENTER_NULL_3,
                        TARGET_M_PRESENTER_BIND_TARGET,
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        "   @Weave(into = \"onDetachedFromWindow\", statement = \"com.example.$T.unbindPresenters(this); this.mIsAttached = false;\")",
                        "   public static void unbindPresenters($V target) {",
                        IF_TARGET_M_PRESENTER_NULL_3,
                        TARGET_M_PRESENTER_UNBIND,
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(presenter, view)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void oneViewBasePresenter() {
        JavaFileObject presenter = file(COM_EXAMPLE, MY_PRESENTER)
                .imports(
                        BasePresenter.class, "P",
                        "com.example.MyView", "V"
                )
                .body(
                        "public class $T extends $P<$V, String> {",
                        "",
                        CLOSING_BRACE_4
                );
        JavaFileObject view = file(COM_EXAMPLE, "MyView")
                .imports(
                        presenter, "MP",
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        PUBLIC_CLASS_T_EXTENDS_VIEW_IMPLEMENTS_V,
                        "",
                        P_MP_M_PRESENTER,
                        "",
                        PUBLIC_T_CONTEXT_C_SUPER_C,
                        "",
                        CLOSING_BRACE_4
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "MyView_Helper")
                .imports(
                        PresenterManager.class, "PM",
                        presenter, "P",
                        view, "V",
                        Object.class,
                        String.class,
                        Weave.class,
                        IllegalStateException.class, "E"
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into = \"<FIELD>\", statement = \"\")",
                        "   private boolean mIsAttached;",
                        "",
                        "   @Weave(into = \"setTag\", args = {\"java.lang.Object\"}, statement = \"String tag = com.example.$T.setPresenters(this, $1); super.setTag(tag); if (this.mIsAttached) { com.example.$T.bindPresenters(this); } return;\")",
                        "   public static String setPresenters($V target, Object tagObject) {",
                        IF_TAG_OBJECT_NULL,
                        IF_TARGET_M_PRESENTER_NULL_1,
                        TARGET_M_PRESENTER_UNBIND,
                        CLOSING_BRACE_3,
                        TARGET_M_PRESENTER_NULL,
                        "           return null;",
                        ELSE,
                        IF_TAG_OBJECT_INSTANCEOF_STRING,
                        THROW_NEW_E_INCORRECT_TYPE_OF_TAG_OBJECT,
                        CLOSING_BRACE_3,
                        STRING_PARAM_STRING_TAG_OBJECT,
                        TARGET_M_PRESENTER_P_PM_GET_TARGET_PARAM_P_CLASS,
                        IF_TARGET_M_PRESENTER_NULL_2,
                        TARGET_M_PRESENTER_NEW_P,
                        PM_PUT_TARGET_PARAM_TARGET_M_PRESENTER,
                        CLOSING_BRACE_3,
                        "           return tagObject.toString();",
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        "   @Weave(into = \"onAttachedToWindow\", statement = \"com.example.$T.bindPresenters(this); this.mIsAttached = true;\")",
                        "   public static void bindPresenters($V target) {",
                        IF_TARGET_M_PRESENTER_NULL_3,
                        TARGET_M_PRESENTER_BIND_TARGET,
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        "   @Weave(into = \"onDetachedFromWindow\", statement = \"com.example.$T.unbindPresenters(this); this.mIsAttached = false;\")",
                        "   public static void unbindPresenters($V target) {",
                        IF_TARGET_M_PRESENTER_NULL_3,
                        TARGET_M_PRESENTER_UNBIND,
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(presenter, view)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void oneActivityPresenter() {
        JavaFileObject viewInterface = file(COM_EXAMPLE, "IMyView")
                .imports(
                        IView.class, "V"
                )
                .body(
                        "public interface $T extends $V {",
                        CLOSING_BRACE_4
                );
        JavaFileObject presenter = file(COM_EXAMPLE, MY_PRESENTER)
                .imports(
                        IPresenter.class, "P",
                        viewInterface, "MV"
                )
                .body(
                        "public class $T implements $P<$MV, String> {",
                        "",
                        String.format(PRESENTER_METHODS, "$MV", STRING),
                        "",
                        CLOSING_BRACE_4
                );
        JavaFileObject activity = file(COM_EXAMPLE, "MyActivity")
                .imports(
                        Activity.class,
                        viewInterface, "MV",
                        Presenter.class, "P",
                        presenter, "MP",
                        Object.class
                )
                .body(
                        "public class $T extends Activity implements $MV {",
                        "",
                        P_MP_M_PRESENTER,
                        "",
                        "   public void setTag(Object o) {}",
                        "",
                        CLOSING_BRACE_4
                );

        JavaFileObject expected = generatedFile(COM_EXAMPLE, "MyActivity_Helper")
                .imports(
                        Weave.class,
                        activity, "A",
                        MvpActivity.class, "M",
                        PresenterManager.class, "PM",
                        presenter, "P",
                        Object.class,
                        String.class,
                        IllegalStateException.class, "E"
                )
                .body(
                        "abstract class $T implements $M {",
                        "",
                        "   @Weave(into = \"setTag\", args = {\"java.lang.Object\"}, statement = \"com.example.$T.setPresenters(this, $1); com.example.$T.bindPresenters(this);\")",
                        "   public static void setPresenters($A target, Object tagObject) {",
                        IF_TAG_OBJECT_NULL,
                        IF_TARGET_M_PRESENTER_NULL_1,
                        TARGET_M_PRESENTER_UNBIND,
                        CLOSING_BRACE_3,
                        TARGET_M_PRESENTER_NULL,
                        ELSE,
                        IF_TAG_OBJECT_INSTANCEOF_STRING,
                        THROW_NEW_E_INCORRECT_TYPE_OF_TAG_OBJECT,
                        CLOSING_BRACE_3,
                        STRING_PARAM_STRING_TAG_OBJECT,
                        TARGET_M_PRESENTER_P_PM_GET_TARGET_PARAM_P_CLASS,
                        IF_TARGET_M_PRESENTER_NULL_2,
                        TARGET_M_PRESENTER_NEW_P,
                        PM_PUT_TARGET_PARAM_TARGET_M_PRESENTER,
                        CLOSING_BRACE_3,
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        "   public static void bindPresenters($A target) {",
                        IF_TARGET_M_PRESENTER_NULL_3,
                        TARGET_M_PRESENTER_BIND_TARGET,
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        "   @Weave(into = \"onDestroy\", statement = \"com.example.$T.unbindPresenters(this);\")",
                        "   public static void unbindPresenters($A target) {",
                        IF_TARGET_M_PRESENTER_NULL_3,
                        TARGET_M_PRESENTER_UNBIND,
                        CLOSING_BRACE_1,
                        CLOSING_BRACE_2,
                        "",
                        CLOSING_BRACE_4
                );

        assertFiles(viewInterface, presenter, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
