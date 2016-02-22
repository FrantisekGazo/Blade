package eu.f3rog.blade.compiler.mvp;

import android.app.Activity;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Blade;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.mvp.MvpActivity;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link PresenterScopeTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public final class PresenterScopeTest extends BaseTest {

    @Test
    public void onePresenter() {
        JavaFileObject activity = file("com.example", "MyActivity")
                .imports(
                        Activity.class,
                        Blade.class, "B"
                )
                .body(
                        "@$B",
                        "public class $T extends Activity {",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyActivity_Helper")
                .imports(
                        MvpActivity.class, "A"
                )
                .body(
                        "abstract class $T implements $A {",
                        "",
                        "}"
                );

        assertFiles(activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
