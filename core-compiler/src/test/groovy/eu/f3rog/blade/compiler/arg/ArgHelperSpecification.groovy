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
import eu.f3rog.blade.core.Bundler
import eu.f3rog.blade.core.Weave
import spock.lang.Unroll

import javax.tools.JavaFileObject
import javax.tools.StandardLocation

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
        try {
            assertFiles(input)
                    .with(BladeProcessor.Module.ARG)
                    .compilesWithoutError()
                    .and()
                    .generatesFileNamed(StandardLocation.CLASS_OUTPUT, "com.example", "MyFragment_Helper.class")
        } catch (AssertionError e) {
            assert e.getMessage().contains("Did not find a generated file corresponding to MyFragment_Helper.class in package com.example")
        }
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
}