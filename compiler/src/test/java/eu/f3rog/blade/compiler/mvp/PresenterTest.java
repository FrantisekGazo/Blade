package eu.f3rog.blade.compiler.mvp;

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
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.core.Weave;

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
            " public <T extends %s> void bind(T view) {} " +
                    " public void unbind() {} " +
                    " public void create(%s o) {} " +
                    " public void destroy() {} " +
                    " public void saveState(Object o) {} " +
                    " public void stateRestored() {} ";

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
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_class_with_Presenter.toString());

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
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_class_with_Presenter.toString());
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
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Presenter.class.getSimpleName()));

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
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Presenter.class.getSimpleName()));

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
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_field_with_annotation.toString(Presenter.class.getSimpleName()));
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
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_Presenter_class.toString());
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
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Inconsistent_Presenter_parameter_classes.toString());
    }

    @Test
    public void onePresenter() {
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
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends View implements $V {",
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
                        "public final class $T {",
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
                        "           target.mPresenter = ($P) $PM.get(target, tagObject, $P.class);",
                        "           if (target.mPresenter == null) {",
                        "               target.mPresenter = new $P();",
                        "               $PM.put(target, tagObject, target.mPresenter);",
                        "               target.mPresenter.create(param);",
                        "           }",
                        "           target.mPresenter.bind(target);",
                        "           return tagObject.toString();",
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

        assertFiles(presenter, view)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void oneBasePresenter() {
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
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T extends View implements $V {",
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
                        "public final class $T {",
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
                        "           target.mPresenter = ($P) $PM.get(target, tagObject, $P.class);",
                        "           if (target.mPresenter == null) {",
                        "               target.mPresenter = new $P();",
                        "               $PM.put(target, tagObject, target.mPresenter);",
                        "               target.mPresenter.create(param);",
                        "           }",
                        "           target.mPresenter.bind(target);",
                        "           return tagObject.toString();",
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

        assertFiles(presenter, view)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
