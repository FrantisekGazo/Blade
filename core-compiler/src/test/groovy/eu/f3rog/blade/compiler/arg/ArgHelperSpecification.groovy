package eu.f3rog.blade.compiler.arg

import android.app.Fragment
import android.os.Bundle
import blade.Arg
import blade.Blade
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.ErrorMsg
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.BundleWrapper
import blade.Bundler
import eu.f3rog.blade.core.Weave
import spock.lang.Unroll

import javax.tools.JavaFileObject


public final class ArgHelperSpecification
        extends BaseSpecification {

    def "fail if @Arg is in invalid class"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T {

                    @#A String mExtraString;
                }
                """,
                [
                        A: Arg.class
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .failsToCompile()
                .withErrorContaining(ArgErrorMsg.Invalid_class_with_Arg)
    }

    @Unroll
    def "fail if @Arg is on #accessor field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment {

                    @#A $accessor String mText;
                }
                """,
                [
                        A: Arg.class,
                        _: [Fragment.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Arg.class.getSimpleName()))

        where:
        accessor    | _
        'private'   | _
        'protected' | _
        'final'     | _
    }

    def "do NOT generate _Helper for a Fragment with only @Blade"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                @#B
                public class #T extends Fragment {}
                """,
                [
                        B: Blade.class,
                        _: [Fragment.class]
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyFragment_Helper",
                BladeProcessor.Module.ARG, input)
    }

    def "generate _Helper for a Fragment with 2 @Arg"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment {

                    @#A String mText;
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        _: [Fragment.class]
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
                       target.mText = args.get("<Arg-mText>", target.mText);
                       target.mNumber = args.get("<Arg-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a Fragment with 2 @Arg - 1 custom Bundler"() {
        given:
        final JavaFileObject customBundler = JavaFile.newFile("com.example", "StringBundler",
                """
                public class #T implements Bundler<String> {

                   public void save(String value, Bundle state) {
                   }

                   public String restore(Bundle state) {
                       return null;
                   }
                }
                """,
                [
                        _: [Bundle.class, Bundler.class]
                ]
        )
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment {

                    @#A(#CB.class) String mText;
                    @#A int mNumber;
                }
                """,
                [
                        A : Arg.class,
                        CB: customBundler,
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
                       #CB mTextBundler = new #CB();
                       target.mText = mTextBundler.restore(args.getBundle("<Arg-mText>"));
                       target.mNumber = args.get("<Arg-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        CB: customBundler,
                        _ : [BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(customBundler, input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a generic Fragment with 2 @Arg"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T<T> extends Fragment {

                    @#A String mText;
                    @#A int mNumber;
                }
                """,
                [
                        A : Arg.class,
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
                    public static <T> void inject(#I<T> target) {
                       if (target.getArguments() == null) {
                           return;
                       }
                       BundleWrapper args = BundleWrapper.from(target.getArguments());
                       target.mText = args.get("<Arg-mText>", target.mText);
                       target.mNumber = args.get("<Arg-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        _ : [BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a generic Fragment field with 2 @Arg"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T<T extends Serializable> extends Fragment {

                    @#A T mData;
                    @#A int mNumber;
                }
                """,
                [
                        A : Arg.class,
                        _ : [Fragment.class, Serializable.class]
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
                    public static <T extends Serializable> void inject(#I<T> target) {
                       if (target.getArguments() == null) {
                           return;
                       }
                       BundleWrapper args = BundleWrapper.from(target.getArguments());
                       target.mData = args.get("<Arg-mData>", target.mData);
                       target.mNumber = args.get("<Arg-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        _ : [BundleWrapper.class, Weave.class, Serializable.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for an inner Fragment class"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyFragment extends Fragment {

                        @#A String mText;
                        @#A int mNumber;
                    }
                }
                """,
                [
                        A : Arg.class,
                        _ : [Fragment.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "Wrapper_MyFragment_Helper",
                """
                abstract class #T {

                    @Weave(
                        into="0^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.inject(this);"
                    )
                    public static void inject(#I.MyFragment target) {
                       if (target.getArguments() == null) {
                           return;
                       }
                       BundleWrapper args = BundleWrapper.from(target.getArguments());
                       target.mText = args.get("<Arg-mText>", target.mText);
                       target.mNumber = args.get("<Arg-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        _ : [BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for 2 inner Fragment classes"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyFragment1 extends Fragment {

                        @#A String mText;
                        @#A int mNumber;
                    }

                    public static class MyFragment2 extends Fragment {

                        @#A String mText;
                        @#A int mNumber;
                    }
                }
                """,
                [
                        A : Arg.class,
                        _ : [Fragment.class]
                ]
        )

        expect:
        final JavaFileObject expected1 = JavaFile.newGeneratedFile("com.example", "Wrapper_MyFragment1_Helper",
                """
                abstract class #T {

                    @Weave(
                        into="0^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.inject(this);"
                    )
                    public static void inject(#I.MyFragment1 target) {
                       if (target.getArguments() == null) {
                           return;
                       }
                       BundleWrapper args = BundleWrapper.from(target.getArguments());
                       target.mText = args.get("<Arg-mText>", target.mText);
                       target.mNumber = args.get("<Arg-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        _ : [BundleWrapper.class, Weave.class]
                ]
        )
        final JavaFileObject expected2 = JavaFile.newGeneratedFile("com.example", "Wrapper_MyFragment2_Helper",
                """
                abstract class #T {

                    @Weave(
                        into="0^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.inject(this);"
                    )
                    public static void inject(#I.MyFragment2 target) {
                       if (target.getArguments() == null) {
                           return;
                       }
                       BundleWrapper args = BundleWrapper.from(target.getArguments());
                       target.mText = args.get("<Arg-mText>", target.mText);
                       target.mNumber = args.get("<Arg-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        _ : [BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2)
    }
}