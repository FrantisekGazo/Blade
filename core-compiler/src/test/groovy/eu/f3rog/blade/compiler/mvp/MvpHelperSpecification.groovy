package eu.f3rog.blade.compiler.mvp

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.view.View
import blade.Blade
import blade.mvp.BasePresenter
import blade.mvp.IPresenter
import blade.mvp.IView
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.Weave
import eu.f3rog.blade.core.WeaveInto
import eu.f3rog.blade.mvp.WeavedMvpActivity
import eu.f3rog.blade.mvp.WeavedMvpFragment
import eu.f3rog.blade.mvp.WeavedMvpView

import javax.inject.Inject
import javax.tools.JavaFileObject

public final class MvpHelperSpecification
        extends BaseSpecification {

    def "ignore field with @Inject that is not a Presenter"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T {

                    @#I Object field;
                }
                """,
                [
                        I: Inject.class
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyClass_Helper",
                BladeProcessor.Module.MVP, input)
    }

    def "ignore Activity (without @Blade) field with @Inject that is not a Presenter"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#I Object field;
                }
                """,
                [
                        I: Inject.class,
                        _: [Activity.class]
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyActivity_Helper",
                BladeProcessor.Module.MVP, input)
    }

    def "ignore Fragment (without @Blade) field with @Inject that is not a Presenter"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Fragment {

                    @#I Object field;
                }
                """,
                [
                        I: Inject.class,
                        _: [Fragment.class]
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyActivity_Helper",
                BladeProcessor.Module.MVP, input)
    }

    def "ignore View (without @Blade) field with @Inject that is not a Presenter"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T extends View {

                    @#I Object field;

                    #T(Context c) {
                        super(c);
                    }
                }
                """,
                [
                        I: Inject.class,
                        _: [Context.class, View.class]
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyClass_Helper",
                BladeProcessor.Module.MVP, input)
    }

    def "ignore constructor with @Inject"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T {

                    @#I #T() {}
                }
                """,
                [
                        I: Inject.class
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyClass_Helper",
                BladeProcessor.Module.MVP, input)
    }

    def "fail if unsupported class type tries to inject a Presenter"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T {

                    @#I #P presenter;
                }
                """,
                [
                        I: Inject.class,
                        P: IPresenter.class
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_injected_Presenter)
    }

    def "fail if Activity (without @Blade) tries to inject a Presenter without implementing IView"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T extends Activity {

                    @#I #P presenter;
                }
                """,
                [
                        I: Inject.class,
                        P: IPresenter.class,
                        _: [Activity.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_injected_Presenter)
    }

    def "fail if Activity (with @Blade) tries to inject a Presenter without implementing IView"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#B
                public class #T extends Activity {

                    @#I #P presenter;
                }
                """,
                [
                        B: Blade.class,
                        I: Inject.class,
                        P: IPresenter.class,
                        _: [Activity.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(String.format(MvpErrorMsg.Invalid_view_class, 'V', 'presenter'))
    }

    def "fail if Fragment tries to inject a Presenter without implementing IView"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T extends Fragment {

                    @#I #P presenter;
                }
                """,
                [
                        I: Inject.class,
                        P: IPresenter.class,
                        _: [Fragment.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_injected_Presenter)
    }

    def "fail if View tries to inject a Presenter without implementing IView"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T extends View {

                    @#I #P presenter;

                    #T(Context context) {
                        super(context);
                    }
                }
                """,
                [
                        I: Inject.class,
                        P: IPresenter.class,
                        _: [Context.class, View.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(MvpErrorMsg.Invalid_class_with_injected_Presenter)
    }

    def "fail if Activity implements incorrect IView"() {
        given:
        final JavaFileObject viewInterface1 = JavaFile.newFile("com.example", "IMyView1",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class,
                ]
        )
        final JavaFileObject viewInterface2 = JavaFile.newFile("com.example", "IMyView2",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class,
                ]
        )
        final JavaFileObject presenter = JavaFile.newFile("com.example", "MyPresenter",
                """
                public class #T extends #BP<#MV2> {
                }
                """,
                [
                        BP : BasePresenter.class,
                        MV2: viewInterface2,
                ]
        )
        final JavaFileObject activity = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity implements #MV1 {

                    @#I #P mPresenter;
                }
                """,
                [
                        I  : Inject.class,
                        MV1: viewInterface1,
                        P  : presenter,
                        _  : [Activity.class]
                ]
        )

        expect:
        assertFiles(viewInterface1, viewInterface2, presenter, activity)
                .with(BladeProcessor.Module.MVP)
                .failsToCompile()
                .withErrorContaining(String.format(MvpErrorMsg.Invalid_view_class, "com.example.IMyView2", "mPresenter"));
    }

    def "ignore if Activity has no @Inject presenter"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class,
                ]
        )
        final JavaFileObject activity = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity implements #MV {
                }
                """,
                [
                        MV: viewInterface,
                        _ : [Activity.class]
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyActivity_Helper",
                BladeProcessor.Module.MVP, viewInterface, activity)
    }

    def "generate _Helper for Activity with @Blade only"() {
        given:
        final JavaFileObject activity = JavaFile.newFile("com.example", "MyActivity",
                """
                @#B
                public class #T extends Activity {
                }
                """,
                [
                        B: Blade.class,
                        _: [Activity.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                @WeaveInto(
                    target = "com.example.#A"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        A: activity,
                        M: WeavedMvpActivity.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for Activity with 1 Presenter"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter = JavaFile.newFile("com.example", "MyPresenter",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject activity = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity implements #V {

                    @#I #P mPresenter;
                }
                """,
                [
                        I: Inject.class,
                        V: viewInterface,
                        P: presenter,
                        _: [Activity.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                @WeaveInto(
                    target = "com.example.#A"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        A: activity,
                        M: WeavedMvpActivity.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for Activity with 2 Presenters"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter1 = JavaFile.newFile("com.example", "MyPresenter1",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject presenter2 = JavaFile.newFile("com.example", "MyPresenter2",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject activity = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity implements #V {

                    @#I #P1 mPresenter1;
                    @#I #P2 mPresenter2;
                }
                """,
                [
                        I : Inject.class,
                        V : viewInterface,
                        P1: presenter1,
                        P2: presenter2,
                        _ : [Activity.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                @WeaveInto(
                    target = "com.example.#A"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        A: activity,
                        M: WeavedMvpActivity.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter1, presenter2, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "ignore Fragment without Presenter"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject fragment = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment implements #V {
                }
                """,
                [
                        V: viewInterface,
                        _: [Fragment.class]
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyFragment_Helper",
                BladeProcessor.Module.MVP, viewInterface, fragment)
    }

    def "generate _Helper for Fragment with 1 Presenter"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter = JavaFile.newFile("com.example", "MyPresenter",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject fragment = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment implements #V {

                    @#I #P mPresenter;
                }
                """,
                [
                        I: Inject.class,
                        V: viewInterface,
                        P: presenter,
                        _: [Fragment.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyFragment_Helper",
                """
                @WeaveInto(
                    target = "com.example.#F"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        F: fragment,
                        M: WeavedMvpFragment.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter, fragment)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for Fragment with 2 Presenters"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter1 = JavaFile.newFile("com.example", "MyPresenter1",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject presenter2 = JavaFile.newFile("com.example", "MyPresenter2",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject fragment = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment implements #V {

                    @#I #P1 mPresenter1;
                    @#I #P2 mPresenter2;
                }
                """,
                [
                        I : Inject.class,
                        V : viewInterface,
                        P1: presenter1,
                        P2: presenter2,
                        _ : [Fragment.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyFragment_Helper",
                """
                @WeaveInto(
                    target = "com.example.#F"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        F: fragment,
                        M: WeavedMvpFragment.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter1, presenter2, fragment)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "ignore View without Presenter"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject view = JavaFile.newFile("com.example", "MyView",
                """
                public class #T extends View implements #V {

                    #T(Context c) {
                        super(c);
                    }
                }
                """,
                [
                        V: viewInterface,
                        _: [Context.class, View.class]
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyView_Helper",
                BladeProcessor.Module.MVP, viewInterface, view)
    }

    def "generate _Helper for View with 1 Presenter"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter = JavaFile.newFile("com.example", "MyPresenter",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject view = JavaFile.newFile("com.example", "MyView",
                """
                public class #T extends View implements #V {

                    @#I #P mPresenter;

                    #T(Context c) {
                        super(c);
                    }
                }
                """,
                [
                        I: Inject.class,
                        V: viewInterface,
                        P: presenter,
                        _: [Context.class, View.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyView_Helper",
                """
                @WeaveInto(
                    target = "com.example.#V"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        V: view,
                        M: WeavedMvpView.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter, view)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for View with 2 Presenters"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter1 = JavaFile.newFile("com.example", "MyPresenter1",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject presenter2 = JavaFile.newFile("com.example", "MyPresenter2",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject view = JavaFile.newFile("com.example", "MyView",
                """
                public class #T extends View implements #V {

                    @#I #P1 mPresenter1;
                    @#I #P2 mPresenter2;

                    #T(Context c) {
                        super(c);
                    }
                }
                """,
                [
                        I : Inject.class,
                        V : viewInterface,
                        P1: presenter1,
                        P2: presenter2,
                        _ : [Context.class, View.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyView_Helper",
                """
                @WeaveInto(
                    target = "com.example.#V"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        V: view,
                        M: WeavedMvpView.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter1, presenter2, view)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for inner Activity with 1 Presenters"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter = JavaFile.newFile("com.example", "MyPresenter",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject activity = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {
                   public static class MyActivity extends Activity implements #V {
                       @#I #P mPresenter;
                   }
                }
                """,
                [
                        I: Inject.class,
                        V: viewInterface,
                        P: presenter,
                        _: [Activity.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "Wrapper_MyActivity_Helper",
                """
                @WeaveInto(
                    target = "com.example.Wrapper.MyActivity"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        M: WeavedMvpActivity.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    def "generate _Helper for inner Fragment with 1 Presenters"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter = JavaFile.newFile("com.example", "MyPresenter",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject fragment = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {
                   public static class MyFragment extends Fragment implements #V {
                       @#I #P mPresenter;
                   }
                }
                """,
                [
                        I: Inject.class,
                        V: viewInterface,
                        P: presenter,
                        _: [Fragment.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "Wrapper_MyFragment_Helper",
                """
                @WeaveInto(
                    target = "com.example.Wrapper.MyFragment"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        M: WeavedMvpFragment.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter, fragment)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    def "generate _Helper for inner View with 1 Presenters"() {
        given:
        final JavaFileObject viewInterface = JavaFile.newFile("com.example", "IMyView",
                """
                public interface #T extends #V {
                }
                """,
                [
                        V: IView.class
                ]
        )
        final JavaFileObject presenter = JavaFile.newFile("com.example", "MyPresenter",
                """
                public class #T extends #BP<#V> {
                }
                """,
                [
                        BP: BasePresenter,
                        V : viewInterface,
                ]
        )
        final JavaFileObject activity = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {
                   public static class MyView extends View implements #V {
                       @#I #P mPresenter;

                       MyView(Context c) {
                           super(c);
                       }
                   }
                }
                """,
                [
                        I: Inject.class,
                        V: viewInterface,
                        P: presenter,
                        _: [Context.class, View.class]
                ]
        )

        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "Wrapper_MyView_Helper",
                """
                @WeaveInto(
                    target = "com.example.Wrapper.MyView"
                )
                abstract class #T implements #M {
                }
                """,
                [
                        M: WeavedMvpView.class,
                        _: [WeaveInto.class]
                ]
        )

        expect:
        assertFiles(viewInterface, presenter, activity)
                .with(BladeProcessor.Module.MVP)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}