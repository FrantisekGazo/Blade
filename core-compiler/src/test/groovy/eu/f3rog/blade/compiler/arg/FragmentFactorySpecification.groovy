package eu.f3rog.blade.compiler.arg

import android.app.Fragment
import android.os.Bundle
import blade.Arg
import blade.Blade
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.BundleWrapper
import blade.Bundler

import javax.tools.JavaFileObject

public final class FragmentFactorySpecification
        extends BaseSpecification {

    def "generate for an Fragment with only @Blade"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                @#B
                public class #T extends Fragment {
                }
                """,
                [
                        B: Blade.class,
                        _: [Fragment.class]
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

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for an Fragment with 1 @Arg"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment {
                    @#A String mText;
                }
                """,
                [
                        A: Arg.class,
                        _: [Fragment.class]
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

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for an Fragment with 2 @Arg - 1 custom Bundler"() {
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

        assertFiles(customBundler, input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for 2 Fragments with @Arg"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "FirstFragment",
                """
                public class #T extends Fragment {
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        _: [Fragment.class]
                ]
        )
        final JavaFileObject input2 = JavaFile.newFile("com.example", "SecondFragment",
                """
                public class #T extends Fragment {
                    @#A String mText;
                    @#A boolean mFlag;
                    @#A double mNumber;
                }
                """,
                [
                        A: Arg.class,
                        _: [Fragment.class]
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

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for 2 Fragments with inherited @Arg"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "BaseFragment",
                """
                public class #T extends Fragment {
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        _: [Fragment.class]
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
                        _: [Fragment.class]
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

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for 2 Fragments with inherited @Arg from abstract class"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "BaseFragment",
                """
                public abstract class #T extends Fragment {
                    @#A int mNumber;
                }
                """,
                [
                        A: Arg.class,
                        _: [Fragment.class]
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
                        _: [Fragment.class]
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

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a generic Fragment with 2 @Arg"() {
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

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a generic Fragment field with 2 @Arg"() {
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

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for an inner Fragment class"() {
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

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for 2 inner Fragment classes"() {
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

        assertFiles(input)
                .with(BladeProcessor.Module.ARG)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }
}
