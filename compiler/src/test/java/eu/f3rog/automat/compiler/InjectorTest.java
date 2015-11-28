package eu.f3rog.automat.compiler;

import android.app.Activity;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaFileObject;

import eu.f3rog.automat.Extra;

import static eu.f3rog.automat.compiler.util.File.file;
import static eu.f3rog.automat.compiler.util.File.generatedFile;

/**
 * Class {@link InjectorTest}
 *
 * @author FrantisekGazo
 * @version 2015-11-27
 */
public class InjectorTest extends BaseTest {
    public final static String[] SUPPORTED = {
            "com.example.$I",
    };

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

        JavaFileObject expected = generatedFile("automat", "Injector")
                .imports(
                        input, "I",
                        "com.example.MainActivity_Injector",
                        Map.class,
                        HashMap.class,
                        Class.class,
                        Object.class
                )
                .body(
                        "public final class $T {",
                        "",
                        "   private static final Map<Class, Object> sInjectors = new HashMap<>();",
                        "",
                        "   public static void inject($I target) {",
                        "       $I_Injector injector;",
                        "       if (sInjectors.containsKey(target.getClass())) {",
                        "           injector = ($I_Injector) sInjectors.get(target.getClass());",
                        "       }",
                        "       else {",
                        "           injector = new $I_Injector();",
                        "           sInjectors.put(target.getClass(), injector);",
                        "       }",
                        "       injector.inject(target);",
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
