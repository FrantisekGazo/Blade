package eu.f3rog.blade.compiler.arg

import android.os.Bundle
import blade.Arg
import blade.Blade
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.MockClass
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.BundleWrapper
import blade.Bundler
import spock.lang.Unroll

import javax.tools.JavaFileObject

public final class FragmentFactorySpecification
        extends BaseSpecification {

    @Unroll
    def "generate for a fragment with only @Blade (where #fragmentClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                @#B
                public class #T extends #F {
                }
                """,
                [
                        B: Blade.class,
                        F: fragmentClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                    public static #I new#I() {
                        #I fragment = new #I();
                        #BW args = new #BW();
                        fragment.setArguments(args.getBundle());
                        return fragment;
                    }
                }
                """,
                [
                        I : input,
                        BW: BundleWrapper.class
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
    def "generate for an fragment with 1 @Arg (where #fragmentClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends #F {
                    @#A String mText;
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                    public static #I new#I(String mText) {
                        #I fragment = new #I();
                        #BW args = new #BW();
                        args.put("<Arg-mText>", mText);
                        fragment.setArguments(args.getBundle());
                        return fragment;
                    }
                }
                """,
                [
                        I : input,
                        BW: BundleWrapper.class,
                        _ : [String.class]
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
    def "generate for an fragment with 2 @Arg - 1 custom Bundler (where #fragmentClassName)"() {
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
                        CB: customBundler,
                        F: fragmentClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                    public static #I new#I(String mText, int mNumber) {
                        #I fragment = new #I();
                        #BW args = new #BW();
                        Bundle mTextBundle = new Bundle();
                        #CB mTextBundler = new #CB();
                        mTextBundler.save(mText, mTextBundle);
                        args.put("<Arg-mText>", mTextBundle);
                        args.put("<Arg-mNumber>", mNumber);
                        fragment.setArguments(args.getBundle());
                        return fragment;
                    }
                }
                """,
                [
                        CB: customBundler,
                        I : input,
                        BW: BundleWrapper.class,
                        _ : [Bundle.class, String.class]
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
    def "generate for 2 fragments with @Arg (where #fragmentClassName)"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "FirstFragment",
                """
                public class #T extends #F {
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
                ]
        )
        final JavaFileObject input2 = JavaFile.newFile("com.example", "SecondFragment",
                """
                public class #T extends #F {
                    @#A String mText;
                    @#A boolean mFlag;
                    @#A double mNumber;
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                    public static #I1 new#I1(int mNumber) {
                        #I1 fragment = new #I1();
                        #BW args = new #BW();
                        args.put("<Arg-mNumber>", mNumber);
                        fragment.setArguments(args.getBundle());
                        return fragment;
                    }

                    public static #I2 new#I2(String mText, boolean mFlag, double mNumber) {
                        #I2 fragment = new #I2();
                        #BW args = new #BW();
                        args.put("<Arg-mText>", mText);
                        args.put("<Arg-mFlag>", mFlag);
                        args.put("<Arg-mNumber>", mNumber);
                        fragment.setArguments(args.getBundle());
                        return fragment;
                    }
                }
                """,
                [
                        I1: input1,
                        I2: input2,
                        BW: BundleWrapper.class,
                        _ : [String.class]
                ]
        )

        assertFiles(fragmentClass, input1, input2)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate for 2 fragments with inherited @Arg (where #fragmentClassName)"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "BaseFragment",
                """
                public class #T extends #F {
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
                ]
        )
        final JavaFileObject input2 = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends #B {
                    @#A String mText;
                }
                """,
                [
                        A: Arg.class,
                        B: input1,
                        F: fragmentClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                    public static #I1 new#I1(int mNumber) {
                        #I1 fragment = new #I1();
                        #BW args = new #BW();
                        args.put("<Arg-mNumber>", mNumber);
                        fragment.setArguments(args.getBundle());
                        return fragment;
                    }

                    public static #I2 new#I2(int mNumber, String mText) {
                        #I2 fragment = new #I2();
                        #BW args = new #BW();
                        args.put("<Arg-mNumber>", mNumber);
                        args.put("<Arg-mText>", mText);
                        fragment.setArguments(args.getBundle());
                        return fragment;
                    }
                }
                """,
                [
                        I1: input1,
                        I2: input2,
                        BW: BundleWrapper.class,
                        _ : [String.class]
                ]
        )

        assertFiles(fragmentClass, input1, input2)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate for 2 fragments with inherited @Arg from abstract class (where #fragmentClassName)"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "BaseFragment",
                """
                public abstract class #T extends #F {
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        F: fragmentClass
                ]
        )
        final JavaFileObject input2 = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends #B {
                    @#A String mText;
                }
                """,
                [
                        A: Arg.class,
                        B: input1,
                        F: fragmentClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                    public static #I2 new#I2(int mNumber, String mText) {
                        #I2 fragment = new #I2();
                        #BW args = new #BW();
                        args.put("<Arg-mNumber>", mNumber);
                        args.put("<Arg-mText>", mText);
                        fragment.setArguments(args.getBundle());
                        return fragment;
                    }
                }
                """,
                [
                        I2: input2,
                        BW: BundleWrapper.class,
                        _ : [String.class]
                ]
        )

        assertFiles(fragmentClass, input1, input2)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [fragmentClassName, fragmentClass] << MockClass.fragmentClasses
    }

    @Unroll
    def "generate for a generic fragment with 2 @Arg (where #fragmentClassName)"() {
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
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                   public static #I new#I(String mText, int mNumber) {
                       #I fragment = new #I();
                       #BW args = new #BW();
                       args.put("<Arg-mText>", mText);
                       args.put("<Arg-mNumber>", mNumber);
                       fragment.setArguments(args.getBundle());
                       return fragment;
                   }
                }
                """,
                [
                        I : input,
                        BW: BundleWrapper.class,
                        _ : [String.class]
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
    def "generate for a generic fragment field with 2 @Arg (where #fragmentClassName)"() {
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
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                   public static #I new#I(Serializable mData, int mNumber) {
                       #I fragment = new #I();
                       #BW args = new #BW();
                       args.put("<Arg-mData>", mData);
                       args.put("<Arg-mNumber>", mNumber);
                       fragment.setArguments(args.getBundle());
                       return fragment;
                   }
                }
                """,
                [
                        I : input,
                        BW: BundleWrapper.class,
                        _ : [Serializable.class]
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
    def "generate for an inner fragment class (where #fragmentClassName)"() {
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
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                   public static #I.MyFragment new#IMyFragment(String mText, int mNumber) {
                       #I.MyFragment fragment = new #I.MyFragment();
                       #BW args = new #BW();
                       args.put("<Arg-mText>", mText);
                       args.put("<Arg-mNumber>", mNumber);
                       fragment.setArguments(args.getBundle());
                       return fragment;
                   }
                }
                """,
                [
                        I : input,
                        BW: BundleWrapper.class,
                        _ : [String.class]
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
    def "generate for 2 inner fragment classes (where #fragmentClassName)"() {
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
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "F",
                """
                public class #T {

                   public static #I.MyFragment1 new#IMyFragment1(String mText, int mNumber) {
                       #I.MyFragment1 fragment = new #I.MyFragment1();
                       #BW args = new #BW();
                       args.put("<Arg-mText>", mText);
                       args.put("<Arg-mNumber>", mNumber);
                       fragment.setArguments(args.getBundle());
                       return fragment;
                   }

                   public static #I.MyFragment2 new#IMyFragment2(String mText, int mNumber) {
                       #I.MyFragment2 fragment = new #I.MyFragment2();
                       #BW args = new #BW();
                       args.put("<Arg-mText>", mText);
                       args.put("<Arg-mNumber>", mNumber);
                       fragment.setArguments(args.getBundle());
                       return fragment;
                   }
                }
                """,
                [
                        I : input,
                        BW: BundleWrapper.class,
                        _ : [String.class]
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
}
