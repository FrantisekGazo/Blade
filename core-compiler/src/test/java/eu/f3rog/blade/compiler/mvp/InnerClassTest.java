package eu.f3rog.blade.compiler.mvp;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.junit.Test;

import javax.inject.Inject;
import javax.tools.JavaFileObject;

import blade.mvp.IPresenter;
import blade.mvp.IView;
import blade.mvp.PresenterManager;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.Weave;
import eu.f3rog.blade.mvp.WeavedMvpActivity;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link InnerClassTest}
 *
 * @author FrantisekGazo
 */
public final class InnerClassTest
        extends BaseTest {

    @Test
    public void inner() {
        JavaFileObject presenter = file("com.example", "MyPresenter")
                .imports(
                        IPresenter.class, "P",
                        "com.example.Wrapper", "V"
                )
                .body(
                        "public class $T implements $P<$V.MyView> {",
                        "",
                        PresenterTest.getPresenterImplementation("$V.MyView"),
                        "}"
                );
        JavaFileObject view = file("com.example", "Wrapper")
                .imports(
                        presenter, "MP",
                        Activity.class,
                        Inject.class, "I",
                        IView.class, "V"
                )
                .body(
                        "public class $T {",
                        "",
                        "   public static class MyView extends Activity implements $V {",
                        "",
                        "       @$I $MP mPresenter;",
                        "",
                        "   }",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "Wrapper_MyView_Helper")
                .imports(
                        WeavedMvpActivity.class, "I"
                )
                .body(
                        "abstract class $T implements $I {",
                        "}"
                );

        assertFiles(presenter, view)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}
