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

    /**
     * android.view.View is missing <code>isAttachedToWindow()</code> method, so this class adds this method.
     */
    private static final JavaFileObject FIXED_ANDROID_VIEW = file("android.view", "FixedView")
            .imports(
                    View.class,
                    Context.class
            )
            .body(
                    "public class $T extends View {",
                    "",
                    "   public $T(Context c) { super(c); } ",
                    "",
                    "   public boolean isAttachedToWindow() {return false;} ",
                    "",
                    "}"
            );

    @Test
    public void invalidClass() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Presenter.class, "P"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$P Object o;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_Presenter);

        input = file("com.example", "MyClass")
                .imports(
                        Presenter.class, "P",
                        View.class
                )
                .body(
                        "public class $T extends View {",
                        "",
                        "   @$P Object o;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_Presenter);
    }

    @Test
    public void invalidField() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends View implements $V {",
                        "",
                        "   @$P private Object o;",
                        "",
                        "   public $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Presenter.class.getSimpleName()));

        input = file("com.example", "MyClass")
                .imports(
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends View implements $V {",
                        "",
                        "   @$P protected Object o;",
                        "",
                        "   public $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Presenter.class.getSimpleName()));

        input = file("com.example", "MyClass")
                .imports(
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends View implements $V {",
                        "",
                        "   @$P final Object o;",
                        "",
                        "   public $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Presenter.class.getSimpleName()));
    }

    @Test
    public void invalidPresenterClass() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends View implements $V {",
                        "",
                        "   @$P Object o;",
                        "",
                        "   public $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_Presenter_class);
    }

    @Test
    public void inconsistentPresenterParameterTypes() {
        JavaFileObject presenter1 = file("com.example", "MyPresenter1")
                .imports(
                        IPresenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T implements $P<$V, Long> {",
                        "",
                        String.format(PRESENTER_METHODS, "$V", "Long"),
                        "",
                        "}"
                );
        JavaFileObject presenter2 = file("com.example", "MyPresenter2")
                .imports(
                        IPresenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T implements $P<$V, String> {",
                        "",
                        String.format(PRESENTER_METHODS, "$V", "String"),
                        "",
                        "}"
                );
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        presenter1, "MP1",
                        presenter2, "MP2",
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends View implements $V {",
                        "",
                        "   @$P $MP1 mPresenter1;",
                        "   @$P $MP2 mPresenter2;",
                        "",
                        "   public $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        assertFiles(presenter1, presenter2, input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Inconsistent_Presenter_parameter_classes);
    }

    @Test
    public void oneViewPresenter() {
        JavaFileObject presenter = file("com.example", "MyPresenter")
                .imports(
                        IPresenter.class, "P",
                        "com.example.MyView", "V"
                )
                .body(
                        "public class $T implements $P<$V, String> {",
                        "",
                        String.format(PRESENTER_METHODS, "$V", "String"),
                        "",
                        "}"
                );
        JavaFileObject view = file("com.example", "MyView")
                .imports(
                        presenter, "MP",
                        FIXED_ANDROID_VIEW, "FV",
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends $FV implements $V {",
                        "",
                        "   @$P $MP mPresenter;",
                        "",
                        "   public $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyView_Helper")
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
                        "   @Weave(into = \"setTag\", args = {\"java.lang.Object\"}, statement = \"String tag = com.example.$T.setPresenters(this, $1); super.setTag(tag); return;\")",
                        "   public static String setPresenters($V target, Object tagObject) {",
                        "       if (tagObject == null) {",
                        "           if (target.mPresenter != null) {",
                        "               target.mPresenter.unbind();",
                        "           }",
                        "           target.mPresenter = null;",
                        "           return null;",
                        "       } else {",
                        "           if (!(tagObject instanceof String)) {",
                        "               throw new $E(\"Incorrect type of tag object.\");",
                        "           }",
                        "           String param = (String) tagObject;",
                        "           target.mPresenter = ($P) $PM.get(target, param, $P.class);",
                        "           if (target.mPresenter == null) {",
                        "               target.mPresenter = new $P();",
                        "               $PM.put(target, param, target.mPresenter);",
                        "           }",
                        "           if (target.isAttachedToWindow()) {",
                        "               target.mPresenter.bind(target);",
                        "           }",
                        "           return tagObject.toString();",
                        "       }",
                        "   }",
                        "",
                        "   @Weave(into = \"onAttachedToWindow\", statement = \"com.example.$T.bindPresenters(this);\")",
                        "   public static void bindPresenters($V target) {",
                        "       if (target.mPresenter != null) {",
                        "           target.mPresenter.bind(target);",
                        "       }",
                        "   }",
                        "",
                        "   @Weave(into = \"onDetachedFromWindow\", statement = \"com.example.$T.unbindPresenters(this);\")",
                        "   public static void unbindPresenters($V target) {",
                        "       if (target.mPresenter != null) {",
                        "           target.mPresenter.unbind();",
                        "       }",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(FIXED_ANDROID_VIEW, presenter, view)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void oneViewBasePresenter() {
        JavaFileObject presenter = file("com.example", "MyPresenter")
                .imports(
                        BasePresenter.class, "P",
                        "com.example.MyView", "V"
                )
                .body(
                        "public class $T extends $P<$V, String> {",
                        "",
                        "}"
                );
        JavaFileObject view = file("com.example", "MyView")
                .imports(
                        presenter, "MP",
                        FIXED_ANDROID_VIEW, "FV",
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends $FV implements $V {",
                        "",
                        "   @$P $MP mPresenter;",
                        "",
                        "   public $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyView_Helper")
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
                        "   @Weave(into = \"setTag\", args = {\"java.lang.Object\"}, statement = \"String tag = com.example.$T.setPresenters(this, $1); super.setTag(tag); return;\")",
                        "   public static String setPresenters($V target, Object tagObject) {",
                        "       if (tagObject == null) {",
                        "           if (target.mPresenter != null) {",
                        "               target.mPresenter.unbind();",
                        "           }",
                        "           target.mPresenter = null;",
                        "           return null;",
                        "       } else {",
                        "           if (!(tagObject instanceof String)) {",
                        "               throw new $E(\"Incorrect type of tag object.\");",
                        "           }",
                        "           String param = (String) tagObject;",
                        "           target.mPresenter = ($P) $PM.get(target, param, $P.class);",
                        "           if (target.mPresenter == null) {",
                        "               target.mPresenter = new $P();",
                        "               $PM.put(target, param, target.mPresenter);",
                        "           }",
                        "           if (target.isAttachedToWindow()) {",
                        "               target.mPresenter.bind(target);",
                        "           }",
                        "           return tagObject.toString();",
                        "       }",
                        "   }",
                        "",
                        "   @Weave(into = \"onAttachedToWindow\", statement = \"com.example.$T.bindPresenters(this);\")",
                        "   public static void bindPresenters($V target) {",
                        "       if (target.mPresenter != null) {",
                        "           target.mPresenter.bind(target);",
                        "       }",
                        "   }",
                        "",
                        "   @Weave(into = \"onDetachedFromWindow\", statement = \"com.example.$T.unbindPresenters(this);\")",
                        "   public static void unbindPresenters($V target) {",
                        "       if (target.mPresenter != null) {",
                        "           target.mPresenter.unbind();",
                        "       }",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(FIXED_ANDROID_VIEW, presenter, view)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void oneActivityPresenter() {
        JavaFileObject viewInterface = file("com.example", "IMyView")
                .imports(
                        IView.class, "V"
                )
                .body(
                        "public interface $T extends $V {",
                        "}"
                );
        JavaFileObject presenter = file("com.example", "MyPresenter")
                .imports(
                        IPresenter.class, "P",
                        viewInterface, "MV"
                )
                .body(
                        "public class $T implements $P<$MV, String> {",
                        "",
                        String.format(PRESENTER_METHODS, "$MV", "String"),
                        "",
                        "}"
                );
        JavaFileObject activity = file("com.example", "MyActivity")
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
                        "   @$P $MP mPresenter;",
                        "",
                        "   public void setTag(Object o) {}",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyActivity_Helper")
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
                        "   @Weave(into = \"setTag\", args = {\"java.lang.Object\"}, statement = \"com.example.$T.setPresenters(this, $1);\")",
                        "   public static void setPresenters($A target, Object tagObject) {",
                        "       if (tagObject == null) {",
                        "           if (target.mPresenter != null) {",
                        "               target.mPresenter.unbind();",
                        "           }",
                        "           target.mPresenter = null;",
                        "       } else {",
                        "           if (!(tagObject instanceof String)) {",
                        "               throw new $E(\"Incorrect type of tag object.\");",
                        "           }",
                        "           String param = (String) tagObject;",
                        "           target.mPresenter = ($P) $PM.get(target, param, $P.class);",
                        "           if (target.mPresenter == null) {",
                        "               target.mPresenter = new $P();",
                        "               $PM.put(target, param, target.mPresenter);",
                        "           }",
                        "           target.mPresenter.bind(target);",
                        "       }",
                        "   }",
                        "",
                        "   @Weave(into = \"onDestroy\", statement = \"com.example.$T.unbindPresenters(this);\")",
                        "   public static void unbindPresenters($A target) {",
                        "       if (target.mPresenter != null) {",
                        "           target.mPresenter.unbind();",
                        "       }",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(viewInterface, presenter, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
