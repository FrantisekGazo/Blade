package eu.f3rog.blade.compiler.mvp;

import android.app.Activity;

import org.junit.Test;

import javax.tools.JavaFileObject;

import blade.Blade;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.Weave;
import eu.f3rog.blade.core.Weaves;

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
                        Weave.class,
                        Weaves.class,
                        String.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   @Weaves({",
                        "       @Weave(into = \"<FIELD>\", statement = \"null\"),",
                        "       @Weave(into = \"getSystemService\", args = {\"java.lang.String\"}, ",
                        "           statement = \"if (blade.mvp.PresenterManager.ACTIVITY_ID.equals($1)) { return this.mActivityId; }\"),",
                        "       @Weave(into = \"onDestroy\", ",
                        "           statement = \"if (this.isFinishing()) { blade.mvp.PresenterManager.removePresentersFor(this); }\"),",
                        "       @Weave(into = \"onCreate\", args = {\"android.os.Bundle\"}, ",
                        "           statement = \"if ($1 != null) { this.mActivityId = $1.getString('blade:activity_id'); blade.mvp.PresenterManager.restorePresentersFor(this, $1); } else { this.mActivityId = java.util.UUID.randomUUID().toString(); }\"),",
                        "       @Weave(into = \"onSaveInstanceState\", args = {\"android.os.Bundle\"}, ",
                        "           statement = \"$1.putString('blade:activity_id', this.mActivityId); blade.mvp.PresenterManager.savePresentersFor(this, $1);\")",
                        "   })",
                        "   private String mActivityId;",
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
