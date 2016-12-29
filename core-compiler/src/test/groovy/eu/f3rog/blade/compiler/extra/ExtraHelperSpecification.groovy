package eu.f3rog.blade.compiler.extra

import android.app.Activity
import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import blade.Blade
import blade.Extra
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

public final class ExtraHelperSpecification
        extends BaseSpecification {

    def "fail if @ is in invalid class"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                public class #T {

                    @#E String mText;
                }
                """,
                [
                        E: Extra.class
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .failsToCompile()
                .withErrorContaining(ExtraErrorMsg.Invalid_class_with_Extra)
    }

    @Unroll
    def "fail if @ is on #accessor field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#E $accessor String mText;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .failsToCompile()
                .withErrorContaining(String.format(ErrorMsg.Invalid_field_with_annotation, Extra.class.getSimpleName()))

        where:
        accessor    | _
        'private'   | _
        'protected' | _
        'final'     | _
    }

    def "do NOT generate _Helper if an Activity has only @Blade"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                @#B
                public class #T extends Activity {}
                """,
                [
                        B: Blade.class,
                        _: [Activity.class]
                ]
        )

        expect:
        try {
            assertFiles(input)
                    .with(BladeProcessor.Module.EXTRA)
                    .compilesWithoutError()
                    .and()
                    .generatesFileNamed(StandardLocation.CLASS_OUTPUT, "com.example", "MyActivity_Helper.class")
        } catch (AssertionError e) {
            assert e.getMessage().contains("Did not find a generated file corresponding to MyActivity_Helper.class in package com.example")
        }
    }

    def "generate _Helper if 2 @ are in an Activity"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#E String mExtraString;
                    @#E int mA;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                abstract class #T {

                    @Weave(
                        into = "0^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.inject(this);"
                    )
                    public static void inject(#I target) {
                        Intent intent = target.getIntent();
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mExtraString = extras.get("<Extra-mExtraString>", target.mExtraString);
                        target.mA = extras.get("<Extra-mA>", target.mA);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper if 2 @ are in an Activity - 1 custom Bundler"() {
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
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#E(#CB.class) String mText;
                    @#E int mA;
                }
                """,
                [
                        E : Extra.class,
                        CB: customBundler,
                        _ : [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                abstract class #T {

                    @Weave(
                        into = "0^onCreate",
                        args = {"android.os.Bundle"},
                        statement = "com.example.#T.inject(this);"
                    )
                    public static void inject(#I target) {
                        Intent intent = target.getIntent();
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        #CB mTextBundler = new #CB();
                        target.mText = mTextBundler.restore(extras.getBundle("<Extra-mText>"));
                        target.mA = extras.get("<Extra-mA>", target.mA);
                    }
                }
                """,
                [
                        I : input,
                        CB: customBundler,
                        _ : [BundleWrapper.class, Intent.class, Weave.class]
                ]
        )

        assertFiles(customBundler, input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper if 2 @ are in an Service"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyService",
                """
                public class #T extends Service {

                    @#E String mExtraString;
                    @#E int mA;

                    public IBinder onBind(Intent intent) {
                        return null;
                    }
                }
                """,
                [
                        E: Extra.class,
                        _: [Service.class, Intent.class, IBinder.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyService_Helper",
                """
                abstract class #T {

                    @Weave(
                        into = "0^onStartCommand",
                        args = {"android.content.Intent", "int", "int"},
                        statement = "com.example.#T.inject(this, \$1);"
                    )
                    public static void inject(#I target, Intent intent) {
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mExtraString = extras.get("<Extra-mExtraString>", target.mExtraString);
                        target.mA = extras.get("<Extra-mA>", target.mA);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper if 2 @ are in an IntentService"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyIntentService",
                """
                public class #T extends IntentService {

                    @#E String mExtraString;
                    @#E int mA;

                    public #T() {
                        super("Test");
                    }

                    @Override
                    protected void onHandleIntent(Intent intent) {
                    }
                }
                """,
                [
                        E: Extra.class,
                        _: [IntentService.class, Intent.class, IBinder.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyIntentService_Helper",
                """
                abstract class #T {

                    @Weave(
                        into = "0^onHandleIntent",
                        args = {"android.content.Intent"},
                        statement = "com.example.#T.inject(this, \$1);"
                    )
                    public static void inject(#I target, Intent intent) {
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mExtraString = extras.get("<Extra-mExtraString>", target.mExtraString);
                        target.mA = extras.get("<Extra-mA>", target.mA);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }
}