package eu.f3rog.blade.compiler.arg

import android.app.Fragment
import android.os.Bundle
import blade.Arg
import blade.Blade
import eu.f3rog.blade.compiler.MockClass
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
    def "fail if @Arg is on a fragment field (where #accessor)"() {
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

    @Unroll
    def "do NOT generate _Helper for a fragment with only @Blade (where #fragmentClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                @#B
                public class #T extends #F {}
                """,
                [
                        B: Blade.class,
                        F: fragmentClass
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyFragment_Helper",
                BladeProcessor.Module.ARG, fragmentClass, input)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate _Helper for a fragment with 2 @Arg (where #fragmentClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends #F {

                    @#A String mText;
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
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

        assertFiles(fragmentClass, input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate _Helper for a fragment with 2 @Arg - 1 custom Bundler (where #fragmentClassName)"() {
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
                public class #T extends #F {

                    @#A(#CB.class) String mText;
                    @#A int mNumber;
                }
                """,
                [
                        A : Arg.class,
                        F : fragmentClass,
                        CB: customBundler
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

        assertFiles(fragmentClass, customBundler, input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate _Helper for a generic fragment with 2 @Arg (where #fragmentClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T<T> extends #F {

                    @#A String mText;
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
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
                        I: input,
                        _: [BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(fragmentClass, input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate _Helper for a generic fragment field with 2 @Arg (where #fragmentClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T<T extends Serializable> extends #F {

                    @#A T mData;
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass,
                        _: [Serializable.class]
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
                        I: input,
                        _: [BundleWrapper.class, Weave.class, Serializable.class]
                ]
        )

        assertFiles(fragmentClass, input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate _Helper for an inner fragment class (where #fragmentClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyFragment extends #F {

                        @#A String mText;
                        @#A int mNumber;
                    }
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
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
                        I: input,
                        _: [BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(fragmentClass, input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate _Helper for 2 inner fragment classes (where #fragmentClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyFragment1 extends #F {

                        @#A String mText;
                        @#A int mNumber;
                    }

                    public static class MyFragment2 extends #F {

                        @#A String mText;
                        @#A int mNumber;
                    }
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
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
                        I: input,
                        _: [BundleWrapper.class, Weave.class]
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
                        I: input,
                        _: [BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(fragmentClass, input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }
}