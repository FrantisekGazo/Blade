package eu.f3rog.blade.compiler.state

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import blade.Blade
import blade.State
import blade.mvp.IPresenter
import blade.mvp.IView
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.ErrorMsg
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.BundleWrapper
import blade.Bundler
import eu.f3rog.blade.core.Weave
import spock.lang.Unroll

import javax.tools.JavaFileObject
import javax.tools.StandardLocation

public final class StateHelperSpecification
        extends BaseSpecification {

    @Unroll
    def "fail if @State is on #accessor field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T {

                    @#S $accessor String mText;
                }
                """,
                [
                        S: State.class,
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, State.class.getSimpleName()))

        where:
        accessor    | _
        'private'   | _
        'protected' | _
        'final'     | _
    }

    def "do NOT generate for a class with only @Blade"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#B
                public class #T {}
                """,
                [
                        B: Blade.class
                ]
        )

        expect:
        try {
            assertFiles(input)
                    .with(BladeProcessor.Module.STATE)
                    .compilesWithoutError()
                    .and()
                    .generatesFileNamed(StandardLocation.CLASS_OUTPUT, "com.example", "MyClass_Helper.class")
        } catch (AssertionError e) {
            assert e.getMessage().contains("Did not find a generated file corresponding to MyClass_Helper.class in package com.example")
        }
    }

    def "generate for a class with 2 @State"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T {

                    @#S String mText;
                    @#S int mNumber;
                }
                """,
                [
                        S: State.class
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                abstract class #T {
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        E: IllegalArgumentException.class,
                        _: [Bundle.class, BundleWrapper.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a class with 2 @State - 1 custom Bundler"() {
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
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T {

                    @#S(#CB.class) String mText;
                    @#S int mNumber;
                }
                """,
                [
                        S : State.class,
                        CB: customBundler
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                abstract class #T {
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        Bundle mTextBundle = new Bundle();
                        #CB mTextBundler = new #CB();
                        mTextBundler.save(target.mText, mTextBundle);
                        bundleWrapper.put("<Stateful-mText>", mTextBundle);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        #CB mTextBundler = new #CB();
                        target.mText = mTextBundler.restore(bundleWrapper.getBundle("<Stateful-mText>"));
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        CB: customBundler,
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [Bundle.class, BundleWrapper.class]
                ]
        )

        assertFiles(customBundler, input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for an Activity with 2 @State"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#S String mText;
                    @#S int mNumber;
                }
                """,
                [
                        S: State.class,
                        _: [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                abstract class #T {
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
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
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
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        E: IllegalArgumentException.class,
                        _: [Bundle.class, BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a Fragment with 2 @State"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyFragment",
                """
                public class #T extends Fragment {

                    @#S String mText;
                    @#S int mNumber;
                }
                """,
                [
                        S: State.class,
                        _: [Fragment.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyFragment_Helper",
                """
                abstract class #T {
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
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
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
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        E: IllegalArgumentException.class,
                        _: [Bundle.class, BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a Presenter with 2 @State"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyPresenter",
                """
                public abstract class #T implements #P<#V> {

                    @#S String mText;
                    @#S int mNumber;
                }
                """,
                [
                        S: State.class,
                        P: IPresenter.class,
                        V: IView.class
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyPresenter_Helper",
                """
                abstract class #T {
                    @Weave(
                        into = "0_onSaveState",
                        args = {"java.lang.Object"},
                        statement = "com.example.#T.saveState(this, (android.os.Bundle) \$1);"
                    )
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    @Weave(
                        into = "0^onCreate",
                        args = {"java.lang.Object"},
                        statement = "com.example.#T.restoreState(this, (android.os.Bundle) \$1);"
                    )
                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        E: IllegalArgumentException.class,
                        _: [Bundle.class, BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a View with 2 @State"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyView",
                """
                public abstract class #T extends View {

                    @#S String mText;
                    @#S int mNumber;

                    public #T(Context c) {
                        super(c);
                    }
                }
                """,
                [
                        S: State.class,
                        _: [Context.class, View.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyView_Helper",
                """
                abstract class #T {
                    @Weave(
                        into = "0^onSaveInstanceState",
                        statement = "android.os.Bundle bundle = new android.os.Bundle();bundle.putParcelable('PARENT_STATE', super.onSaveInstanceState());com.example.#T.saveState(this, bundle);return bundle;"
                    )
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    @Weave(
                        into = "0^onRestoreInstanceState",
                        args = {"android.os.Parcelable"},
                        statement = "if (\$1 instanceof android.os.Bundle) {android.os.Bundle bundle = (android.os.Bundle) \$1;com.example.#T.restoreState(this, bundle);super.onRestoreInstanceState(bundle.getParcelable('PARENT_STATE'));} else {super.onRestoreInstanceState(\$1);}return;"
                    )
                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        E: IllegalArgumentException.class,
                        _: [Bundle.class, BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a View with 2 @State with onSaveInstanceState()"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyView",
                """
                public abstract class #T extends View {

                    @#S String mText;
                    @#S int mNumber;

                    public #T(Context c) {
                        super(c);
                    }

                    @Override
                    protected Parcelable onSaveInstanceState() {
                        return null;
                    }
                }
                """,
                [
                        S: State.class,
                        _: [Context.class, Override.class, Parcelable.class, View.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyView_Helper",
                """
                abstract class #T {
                    @Weave(
                        into = "0^onSaveInstanceState/onSaveInstanceState_BladeState",
                        statement = "android.os.Bundle bundle = new android.os.Bundle();bundle.putParcelable('USER_STATE', this.onSaveInstanceState_BladeState());com.example.#T.saveState(this, bundle);return bundle;"
                    )
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    @Weave(
                        into = "0^onRestoreInstanceState",
                        args = {"android.os.Parcelable"},
                        statement = "if (\$1 instanceof android.os.Bundle) {android.os.Bundle bundle = (android.os.Bundle) \$1;com.example.#T.restoreState(this, bundle);super.onRestoreInstanceState(bundle.getParcelable('PARENT_STATE'));} else {super.onRestoreInstanceState(\$1);}return;"
                    )
                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        E: IllegalArgumentException.class,
                        _: [Bundle.class, BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a View with 2 @State with onRestoreInstanceState()"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyView",
                """
                public abstract class #T extends View {

                    @#S String mText;
                    @#S int mNumber;

                    public #T(Context c) {
                        super(c);
                    }

                    @Override
                    protected void onRestoreInstanceState(Parcelable s) {
                    }
                }
                """,
                [
                        S: State.class,
                        _: [Context.class, Override.class, Parcelable.class, View.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyView_Helper",
                """
                abstract class #T {
                    @Weave(
                        into = "0^onSaveInstanceState",
                        statement = "android.os.Bundle bundle = new android.os.Bundle();bundle.putParcelable('PARENT_STATE', super.onSaveInstanceState());com.example.#T.saveState(this, bundle);return bundle;"
                    )
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    @Weave(
                        into = "0^onRestoreInstanceState/onRestoreInstanceState_BladeState",
                        args = {"android.os.Parcelable"},
                        statement = "if (\$1 instanceof android.os.Bundle) {android.os.Bundle bundle = (android.os.Bundle) \$1;com.example.#T.restoreState(this, bundle);this.onRestoreInstanceState_BladeState(bundle.getParcelable('USER_STATE'));} else {this.onRestoreInstanceState_BladeState(\$1);}return;"
                    )
                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        E: IllegalArgumentException.class,
                        _: [Bundle.class, BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a View with 2 @State with onSaveInstanceState() and onRestoreInstanceState()"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyView",
                """
                public abstract class #T extends View {

                    @#S String mText;
                    @#S int mNumber;

                    public #T(Context c) {
                        super(c);
                    }

                    @Override
                    protected Parcelable onSaveInstanceState() {
                        return null;
                    }

                    @Override
                    protected void onRestoreInstanceState(Parcelable s) {
                    }
                }
                """,
                [
                        S: State.class,
                        _: [Context.class, Override.class, Parcelable.class, View.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyView_Helper",
                """
                abstract class #T {
                    @Weave(
                        into = "0^onSaveInstanceState/onSaveInstanceState_BladeState",
                        statement = "android.os.Bundle bundle = new android.os.Bundle();bundle.putParcelable('USER_STATE', this.onSaveInstanceState_BladeState());com.example.#T.saveState(this, bundle);return bundle;"
                    )
                    public static void saveState(#I target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    @Weave(
                        into = "0^onRestoreInstanceState/onRestoreInstanceState_BladeState",
                        args = {"android.os.Parcelable"},
                        statement = "if (\$1 instanceof android.os.Bundle) {android.os.Bundle bundle = (android.os.Bundle) \$1;com.example.#T.restoreState(this, bundle);this.onRestoreInstanceState_BladeState(bundle.getParcelable('USER_STATE'));} else {this.onRestoreInstanceState_BladeState(\$1);}return;"
                    )
                    public static void restoreState(#I target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        E: IllegalArgumentException.class,
                        _: [Bundle.class, BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a generic Activity with 2 @Extra"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T<T> {

                    @#S String mText;
                    @#S int mNumber;
                }
                """,
                [
                        S : State.class,
                        _ : []
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                abstract class #T {

                    public static <T> void saveState(#I<T> target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    public static <T> void restoreState(#I<T> target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [Bundle.class, BundleWrapper.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a generic Activity field with 2 @Extra"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T<T extends Serializable> {

                    @#S T mData;
                    @#S int mNumber;
                }
                """,
                [
                        S : State.class,
                        _ : [Serializable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                abstract class #T {

                    public static <T extends Serializable> void saveState(#I<T> target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mData>", target.mData);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    public static <T extends Serializable> void restoreState(#I<T> target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mData = bundleWrapper.get("<Stateful-mData>", target.mData);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [Bundle.class, BundleWrapper.class, Serializable.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for an inner View class"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyView extends View {

                        @#S String mText;
                        @#S int mNumber;

                        public MyView(Context c) {
                            super(c);
                        }
                    }
                }
                """,
                [
                        S : State.class,
                        _ : [Context.class, View.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "Wrapper_MyView_Helper",
                """
                abstract class #T {

                    @Weave(
                        into = "0^onSaveInstanceState",
                        statement = "android.os.Bundle bundle = new android.os.Bundle();bundle.putParcelable('PARENT_STATE', super.onSaveInstanceState());com.example.#T.saveState(this, bundle);return bundle;"
                    )
                    public static void saveState(#I.MyView target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    @Weave(
                        into = "0^onRestoreInstanceState",
                        args = {"android.os.Parcelable"},
                        statement = "if (\$1 instanceof android.os.Bundle) {android.os.Bundle bundle = (android.os.Bundle) \$1;com.example.#T.restoreState(this, bundle);super.onRestoreInstanceState(bundle.getParcelable('PARENT_STATE'));} else {super.onRestoreInstanceState(\$1);}return;"
                    )
                    public static void restoreState(#I.MyView target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [Bundle.class, BundleWrapper.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for an inner class"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyClass {

                        @#S String mText;
                        @#S int mNumber;
                    }
                }
                """,
                [
                        S : State.class,
                        _ : []
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "Wrapper_MyClass_Helper",
                """
                abstract class #T {

                    public static void saveState(#I.MyClass target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    public static void restoreState(#I.MyClass target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [Bundle.class, BundleWrapper.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for 2 inner classes"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyClass1 {

                        @#S String mText;
                        @#S int mNumber;
                    }

                    public static class MyClass2 {

                        @#S String mText;
                        @#S int mNumber;
                    }
                }
                """,
                [
                        S : State.class,
                        _ : []
                ]
        )

        expect:
        final JavaFileObject expected1 = JavaFile.newGeneratedFile("com.example", "Wrapper_MyClass1_Helper",
                """
                abstract class #T {

                    public static void saveState(#I.MyClass1 target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    public static void restoreState(#I.MyClass1 target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [Bundle.class, BundleWrapper.class]
                ]
        )
        final JavaFileObject expected2 = JavaFile.newGeneratedFile("com.example", "Wrapper_MyClass2_Helper",
                """
                abstract class #T {

                    public static void saveState(#I.MyClass2 target, Bundle state) {
                        if (state == null) {
                            throw new #E("State cannot be null!");
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        bundleWrapper.put("<Stateful-mText>", target.mText);
                        bundleWrapper.put("<Stateful-mNumber>", target.mNumber);
                    }

                    public static void restoreState(#I.MyClass2 target, Bundle state) {
                        if (state == null) {
                            return;
                        }
                        BundleWrapper bundleWrapper = BundleWrapper.from(state);
                        target.mText = bundleWrapper.get("<Stateful-mText>", target.mText);
                        target.mNumber = bundleWrapper.get("<Stateful-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I : input,
                        E : IllegalArgumentException.class,
                        _ : [Bundle.class, BundleWrapper.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.STATE)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2)
    }
}