package eu.f3rog.blade.compiler.combination

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import blade.Arg
import blade.Extra
import blade.State
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.BundleWrapper
import eu.f3rog.blade.core.Weave
import spock.lang.Unroll

import javax.tools.JavaFileObject

public final class CombinedSpecification
        extends BaseSpecification {

    @Unroll
    def "generate _Helper for a class with a @Arg+@State field (#annotations)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment {

                    $annotations int number;
                }
                """,
                [
                        A : Arg.class,
                        S : State.class,
                        _ : [Fragment.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyFragment_Helper",
                """
                abstract class #T {

                    @Weave(
                        into="0^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.inject(this);"
                    )
                    public static void inject(#I target) {
                        if (target.getArguments() == null) {
                            return;
                        }
                        BundleWrapper args = BundleWrapper.from(target.getArguments());
                        target.number = args.get("<Arg-number>", target.number);
                    }

                    @Weave(
                        into = "0_onSaveInstanceState",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.saveState(this, \$1);"
                    )
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-number>", target.number);
                    }

                    @Weave(
                        into = "1^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.restoreState(this, \$1);"
                    )
                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.number = bundleWrapper.get("<Stateful-number>", target.number);
                    }
                }
                """,
                [
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [
                                Bundle.class,
                                BundleWrapper.class,
                                Weave.class
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.ARG, BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        annotations   | _
        '@Arg @State' | _
        '@State @Arg' | _
    }

    @Unroll
    def "generate _Helper for a class with a @Extra+@State field (#annotations)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    $annotations int number;
                }
                """,
                [
                        E : Extra.class,
                        S : State.class,
                        _ : [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                abstract class #T {

                    @Weave(
                        into="0^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.inject(this);"
                    )
                    public static void inject(#I target) {
                        Intent intent = target.getIntent();
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.number = extras.get("<Extra-number>", target.number);
                    }

                    @Weave(
                        into = "0_onSaveInstanceState",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.saveState(this, \$1);"
                    )
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-number>", target.number);
                    }

                    @Weave(
                        into = "1^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.restoreState(this, \$1);"
                    )
                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.number = bundleWrapper.get("<Stateful-number>", target.number);
                    }
                }
                """,
                [
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [
                                Bundle.class,
                                BundleWrapper.class,
                                Intent.class,
                                Weave.class
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA, BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        annotations   | _
        '@Extra @State' | _
        '@State @Extra' | _
    }
}