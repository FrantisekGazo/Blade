package eu.f3rog.blade.compiler.mvp;

import android.content.Context;
import android.view.View;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Presenter;
import blade.mvp.IPresenter;
import blade.mvp.IView;
import blade.mvp.PresenterManager;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
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
        JavaFileObject presenter = file("com.example", "MyPresenter")
                .imports(
                        IPresenter.class, "P",
                        "com.example.Wrapper", "V"
                )
                .body(
                        "public class $T implements $P<$V.MyView, String> {",
                        "",
                        PresenterTest.getPresenterImplementation("$V.MyView", "String"),
                        "}"
                );
        JavaFileObject view = file("com.example", "Wrapper")
                .imports(
                        presenter, "MP",
                        View.class,
                        Context.class,
                        Presenter.class, "P",
                        IView.class, "V"
                )
                .body(
                        "public class $T {",
                        "",
                        "   public static class MyView extends View implements $V {",
                        "",
                        "       @$P $MP mPresenter;",
                        "",
                        "       public MyView(Context c) {super(c);}",
                        "   }",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "Wrapper_MyView_Helper")
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
                        "   @Weave(into = \"^setTag\", args = {\"java.lang.Object\"}, statement = \"String tag = com.example.$T.setPresenters(this, $1); super.setTag(tag); if (this.mIsAttached) { com.example.$T.bindPresenters(this); } return;\")",
                        "   public static String setPresenters($V.MyView target, Object tagObject) {",
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
                        "           return tagObject.toString();",
                        "       }",
                        "   }",
                        "",
                        "   @Weave(into = \"^onAttachedToWindow\", statement = \"com.example.$T.bindPresenters(this); this.mIsAttached = true;\")",
                        "   public static void bindPresenters($V.MyView target) {",
                        "       if (target.mPresenter != null) {",
                        "           target.mPresenter.bind(target);",
                        "       }",
                        "   }",
                        "",
                        "   @Weave(into = \"^onDetachedFromWindow\", statement = \"com.example.$T.unbindPresenters(this); this.mIsAttached = false;\")",
                        "   public static void unbindPresenters($V.MyView target) {",
                        "       if (target.mPresenter != null) {",
                        "           target.mPresenter.unbind();",
                        "       }",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(presenter, view)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}
