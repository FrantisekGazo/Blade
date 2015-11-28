package eu.f3rog.automat.compiler;

import android.app.Activity;
import android.os.Bundle;

import org.junit.Test;

import javax.tools.JavaFileObject;

import eu.f3rog.automat.Extra;

import static eu.f3rog.automat.compiler.util.File.file;
import static eu.f3rog.automat.compiler.util.File.generatedFile;

/**
 * Class {@link ActivityInjectorTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class ActivityInjectorTest extends BaseTest {

    @Test
    public void invalidCLass() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Invalid_class_with_Extra.toString());
    }
    @Test
    public void invalidField() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E private String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Extra_cannot_be_private_or_protected.toString());

        input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E protected String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Extra_cannot_be_private_or_protected.toString());

        input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$E final String mExtraString;",
                        "",
                        "}"
                );

        assertFiles(input)
                .failsToCompile()
                .withErrorContaining(ErrorMsg.Extra_cannot_be_final.toString());
    }

    @Test
    public void test1() {
        JavaFileObject input = file("com.example", "MainActivity")
                .imports(
                        Extra.class, "E",
                        Activity.class
                )
                .body(
                        "public class $T extends Activity {",
                        "",
                        "   @$E String mExtraString;",
                        "   @$E int mA;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MainActivity_Injector")
                .imports(
                        input, "I",
                        Bundle.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   public void inject($I target) {",
                        "       if (target.getIntent() == null || target.getIntent().getExtras() == null) {",
                        "           return;",
                        "       }",
                        "       Bundle extras = target.getIntent().getExtras();",
                        "       target.mExtraString = extras.getString(\"com.example.$I-mExtraString\");",
                        "       target.mA = extras.getInt(\"com.example.$I-mA\");",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

}
