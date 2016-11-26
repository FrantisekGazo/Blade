package eu.f3rog.blade.compiler.mvp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.View;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.tools.JavaFileObject;

import blade.mvp.IPresenter;
import blade.mvp.IView;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.mvp.WeavedMvpActivity;
import eu.f3rog.blade.mvp.WeavedMvpFragment;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link PresenterTest}
 *
 * @author FrantisekGazo
 */
public final class PresenterTest extends BaseTest {

    static String getPresenterImplementation(String viewType) {
        return String.format(PRESENTER_METHODS, viewType, viewType);
    }

    private static final String PRESENTER_METHODS =
            " public void onBind(%s view) {} " +
                    " public %s getView() {return null;} " +
                    " public void onUnbind() {} " +
                    " public void onCreate(Object state) {} " +
                    " public void onDestroy() {} " +
                    " public void onSaveState(Object state) {} ";

    @Test
    public void ignoredInjections() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Inject.class, "I"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$I Object o;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError();

        assertFileNotGenerated("com.example", "MyClass_Helper", input);

        input = file("com.example", "MyClass")
                .imports(
                        Inject.class, "I",
                        View.class,
                        Context.class
                )
                .body(
                        "public class $T extends View {",
                        "",
                        "   @$I Object o;",
                        "",
                        "   $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError();

        assertFileNotGenerated("com.example", "MyClass_Helper", input);
    }

    @Test
    public void invalidClassWithInjectedPresenter() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Inject.class, "I",
                        IPresenter.class, "P"
                )
                .body(
                        "public class $T {",
                        "",
                        "   @$I $P o;",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_injected_Presenter);

        input = file("com.example", "MyClass")
                .imports(
                        Inject.class, "I",
                        IPresenter.class, "P",
                        View.class,
                        Context.class
                )
                .body(
                        "public class $T extends View {",
                        "",
                        "   @$I $P o;",
                        "",
                        "   $T(Context c) {super(c);}",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_injected_Presenter);
    }

    @Test
    public void activityWithoutPresenter() {
        JavaFileObject viewInterface = file("com.example", "IMyView")
                .imports(
                        IView.class, "V"
                )
                .body(
                        "public interface $T extends $V {",
                        "}"
                );
        JavaFileObject activity = file("com.example", "MyActivity")
                .imports(
                        Activity.class,
                        viewInterface, "MV",
                        Inject.class, "I"
                )
                .body(
                        "public class $T extends Activity implements $MV {",
                        "}"
                );

        assertFiles(viewInterface, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError();

        assertFileNotGenerated("com.example", "MyActivity_Helper", viewInterface, activity);
    }

    @Test
    public void activityWith1Presenter() {
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
                        "public class $T implements $P<$MV> {",
                        "",
                        getPresenterImplementation("$MV"),
                        "",
                        "}"
                );
        JavaFileObject activity = file("com.example", "MyActivity")
                .imports(
                        Activity.class,
                        viewInterface, "MV",
                        Inject.class, "I",
                        presenter, "MP"
                )
                .body(
                        "public class $T extends Activity implements $MV {",
                        "",
                        "   @$I $MP mPresenter;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyActivity_Helper")
                .imports(
                        WeavedMvpActivity.class, "M"
                )
                .body(
                        "abstract class $T implements $M {",
                        "}"
                );

        assertFiles(viewInterface, presenter, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void activityWith2Presenters() {
        JavaFileObject viewInterface = file("com.example", "IMyView")
                .imports(
                        IView.class, "V"
                )
                .body(
                        "public interface $T extends $V {",
                        "}"
                );
        JavaFileObject presenter1 = file("com.example", "MyPresenter1")
                .imports(
                        IPresenter.class, "P",
                        viewInterface, "MV"
                )
                .body(
                        "public class $T implements $P<$MV> {",
                        "",
                        getPresenterImplementation("$MV"),
                        "",
                        "}"
                );
        JavaFileObject presenter2 = file("com.example", "MyPresenter2")
                .imports(
                        IPresenter.class, "P",
                        viewInterface, "MV"
                )
                .body(
                        "public class $T implements $P<$MV> {",
                        "",
                        getPresenterImplementation("$MV"),
                        "",
                        "}"
                );
        JavaFileObject activity = file("com.example", "MyActivity")
                .imports(
                        Activity.class,
                        viewInterface, "MV",
                        Inject.class, "I",
                        presenter1, "MP1",
                        presenter2, "MP2"
                )
                .body(
                        "public class $T extends Activity implements $MV {",
                        "",
                        "   @$I $MP1 mPresenter1;",
                        "   @$I $MP2 mPresenter2;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyActivity_Helper")
                .imports(
                        WeavedMvpActivity.class, "M"
                )
                .body(
                        "abstract class $T implements $M {",
                        "}"
                );

        assertFiles(viewInterface, presenter1, presenter2, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void fragmentWithoutPresenter() {
        JavaFileObject viewInterface = file("com.example", "IMyView")
                .imports(
                        IView.class, "V"
                )
                .body(
                        "public interface $T extends $V {",
                        "}"
                );
        JavaFileObject fragment = file("com.example", "MyFragment")
                .imports(
                        Fragment.class,
                        viewInterface, "MV",
                        Inject.class, "I"
                )
                .body(
                        "public class $T extends Fragment implements $MV {",
                        "}"
                );

        assertFiles(viewInterface, fragment)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError();

        assertFileNotGenerated("com.example", "MyFragment_Helper", viewInterface, fragment);
    }

    @Test
    public void fragmentWith1Presenter() {
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
                        "public class $T implements $P<$MV> {",
                        "",
                        getPresenterImplementation("$MV"),
                        "",
                        "}"
                );
        JavaFileObject fragment = file("com.example", "MyFragment")
                .imports(
                        Fragment.class,
                        viewInterface, "MV",
                        Inject.class, "I",
                        presenter, "MP"
                )
                .body(
                        "public class $T extends Fragment implements $MV {",
                        "",
                        "   @$I $MP mPresenter;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyFragment_Helper")
                .imports(
                        WeavedMvpFragment.class, "M"
                )
                .body(
                        "abstract class $T implements $M {",
                        "}"
                );

        assertFiles(viewInterface, presenter, fragment)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void fragmentWith2Presenters() {
        JavaFileObject viewInterface = file("com.example", "IMyView")
                .imports(
                        IView.class, "V"
                )
                .body(
                        "public interface $T extends $V {",
                        "}"
                );
        JavaFileObject presenter1 = file("com.example", "MyPresenter1")
                .imports(
                        IPresenter.class, "P",
                        viewInterface, "MV"
                )
                .body(
                        "public class $T implements $P<$MV> {",
                        "",
                        getPresenterImplementation("$MV"),
                        "",
                        "}"
                );
        JavaFileObject presenter2 = file("com.example", "MyPresenter2")
                .imports(
                        IPresenter.class, "P",
                        viewInterface, "MV"
                )
                .body(
                        "public class $T implements $P<$MV> {",
                        "",
                        getPresenterImplementation("$MV"),
                        "",
                        "}"
                );
        JavaFileObject fragment = file("com.example", "MyFragment")
                .imports(
                        Fragment.class,
                        viewInterface, "MV",
                        Inject.class, "I",
                        presenter1, "MP1",
                        presenter2, "MP2"
                )
                .body(
                        "public class $T extends Fragment implements $MV {",
                        "",
                        "   @$I $MP1 mPresenter1;",
                        "   @$I $MP2 mPresenter2;",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyFragment_Helper")
                .imports(
                        WeavedMvpFragment.class, "M"
                )
                .body(
                        "abstract class $T implements $M {",
                        "}"
                );

        assertFiles(viewInterface, presenter1, presenter2, fragment)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    private void assertFileNotGenerated(String pack, String className, JavaFileObject... files) {
        try {
            JavaFileObject expected = generatedFile(pack, className)
                    .imports()
                    .body("class $T {}");

            // 'expected' file should not be created, because class does not contain any Presenter.
            // compile should throw AssertionError.
            assertFiles(files)
                    .with(BladeProcessor.Module.MVP)
                    .compilesWithoutError()
                    .and()
                    .generatesSources(expected);

            Assert.assertTrue(false);
        } catch (AssertionError e) {
            String message = e.getMessage();
            Assert.assertTrue(
                    message.contains("An expected source declared one or more top-level types that were not present.")
            );
            Assert.assertTrue(
                    message.contains(String.format("Expected top-level types: <[%s.%s]>", pack, className))
            );
        }
    }
}
