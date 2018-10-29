package eu.f3rog.blade.compiler.extra

import android.app.Activity
import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import blade.Blade
import blade.Bundler
import blade.Extra
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.ErrorMsg
import eu.f3rog.blade.compiler.MockClass
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.BundleWrapper
import eu.f3rog.blade.core.Weave
import eu.f3rog.blade.core.Weaves
import spock.lang.Unroll

import javax.tools.JavaFileObject

public final class ExtraHelperSpecification
        extends BaseSpecification {

    def "fail if @Extra is in invalid class"() {
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
    def "fail if @Extra is on a field (where #accessor)"() {
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

    @Unroll
    def "do NOT generate _Helper if an activity class has only @Blade (where #activityClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                @#B
                public class #T extends #A {}
                """,
                [
                        B: Blade.class,
                        A: activityClass
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("com.example", "MyActivity_Helper", BladeProcessor.Module.EXTRA, input, activityClass)

        where:
        [activityClassName, activityClass] << MockClass.activityClasses
    }

    @Unroll
    def "generate _Helper if 1 @Extra is in an activity class (where #type)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#E $type mFlag;
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

                    @Weaves({
                        @Weave(
                            into="0^onCreate",
                            args = {"android.os.Bundle"},
                            statement = "com.example.#T.inject(this, this.getIntent());"
                        ),
                        @Weave(
                            into="0^onNewIntent",
                            args = {"android.content.Intent"},
                            statement = "com.example.#T.inject(this, \$1);"
                        )
                    })
                    public static void inject(#I target, Intent intent) {
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mFlag = extras.get("<Extra-mFlag>", target.mFlag);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Weaves.class, Weave.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        type      | _
        'byte'    | _
        'boolean' | _
        'int'     | _
        'float'   | _
        'double'  | _
        'String'  | _
    }

    @Unroll
    def "generate _Helper if 2 @Extra are in an activity class (where #activityClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends #A {

                    @#E String mExtraString;
                    @#E int mA;
                }
                """,
                [
                        E: Extra.class,
                        A: activityClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                abstract class #T {

                    @Weaves({
                        @Weave(
                            into="0^onCreate",
                            args = {"android.os.Bundle"},
                            statement = "com.example.#T.inject(this, this.getIntent());"
                        ),
                        @Weave(
                            into="0^onNewIntent",
                            args = {"android.content.Intent"},
                            statement = "com.example.#T.inject(this, \$1);"
                        )
                    })
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
                        _: [BundleWrapper.class, Intent.class, Weaves.class, Weave.class]
                ]
        )

        assertFiles(activityClass, input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [activityClassName, activityClass] << MockClass.activityClasses
    }

    @Unroll
    def "generate _Helper if 2 @Extra are in an activity class - 1 custom Bundler (where #activityClassName)"() {
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
                public class #T extends #A {

                    @#E(#CB.class) String mText;
                    @#E int mA;
                }
                """,
                [
                        E : Extra.class,
                        CB: customBundler,
                        A : activityClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                abstract class #T {

                    @Weaves({
                        @Weave(
                            into="0^onCreate",
                            args = {"android.os.Bundle"},
                            statement = "com.example.#T.inject(this, this.getIntent());"
                        ),
                        @Weave(
                            into="0^onNewIntent",
                            args = {"android.content.Intent"},
                            statement = "com.example.#T.inject(this, \$1);"
                        )
                    })
                    public static void inject(#I target, Intent intent) {
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
                        _ : [BundleWrapper.class, Intent.class, Weaves.class, Weave.class]
                ]
        )

        assertFiles(customBundler, input, activityClass)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [activityClassName, activityClass] << MockClass.activityClasses
    }

    def "generate _Helper if 2 @Extra are in a Service class"() {
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

    def "generate _Helper if 2 @Extra are in an IntentService class"() {
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

    @Unroll
    def "generate _Helper if 2 @Extra are in a generic activity class (where #activityClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T<T> extends #A {

                    @#E String mText;
                    @#E int mNumber;
                }
                """,
                [
                        E: Extra.class,
                        A: activityClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                abstract class #T {

                    @Weaves({
                        @Weave(
                            into="0^onCreate",
                            args = {"android.os.Bundle"},
                            statement = "com.example.#T.inject(this, this.getIntent());"
                        ),
                        @Weave(
                            into="0^onNewIntent",
                            args = {"android.content.Intent"},
                            statement = "com.example.#T.inject(this, \$1);"
                        )
                    })
                    public static <T> void inject(#I<T> target, Intent intent) {
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mText = extras.get("<Extra-mText>", target.mText);
                        target.mNumber = extras.get("<Extra-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Weaves.class, Weave.class]
                ]
        )

        assertFiles(input, activityClass)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [activityClassName, activityClass] << MockClass.activityClasses
    }

    @Unroll
    def "generate _Helper if 2 @Extra are in a generic activity class where T extends Serializable (where #activityClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T<T extends Serializable> extends #A {

                    @#E T mData;
                    @#E int mNumber;
                }
                """,
                [
                        E: Extra.class,
                        A: activityClass,
                        _: [Serializable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyActivity_Helper",
                """
                abstract class #T {

                    @Weaves({
                        @Weave(
                            into="0^onCreate",
                            args = {"android.os.Bundle"},
                            statement = "com.example.#T.inject(this, this.getIntent());"
                        ),
                        @Weave(
                            into="0^onNewIntent",
                            args = {"android.content.Intent"},
                            statement = "com.example.#T.inject(this, \$1);"
                        )
                    })
                    public static <T extends Serializable> void inject(#I<T> target, Intent intent) {
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mData = extras.get("<Extra-mData>", target.mData);
                        target.mNumber = extras.get("<Extra-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Serializable.class, Weaves.class, Weave.class]
                ]
        )

        assertFiles(input, activityClass)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [activityClassName, activityClass] << MockClass.activityClasses
    }

    @Unroll
    def "generate _Helper if 2 @Extra are in an inner activity class (where #activityClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyActivity extends #A {

                        @#E String mText;
                        @#E int mNumber;
                    }
                }
                """,
                [
                        E: Extra.class,
                        A: activityClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "Wrapper_MyActivity_Helper",
                """
                abstract class #T {

                    @Weaves({
                        @Weave(
                            into="0^onCreate",
                            args = {"android.os.Bundle"},
                            statement = "com.example.#T.inject(this, this.getIntent());"
                        ),
                        @Weave(
                            into="0^onNewIntent",
                            args = {"android.content.Intent"},
                            statement = "com.example.#T.inject(this, \$1);"
                        )
                    })
                    public static void inject(#I.MyActivity target, Intent intent) {
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mText = extras.get("<Extra-mText>", target.mText);
                        target.mNumber = extras.get("<Extra-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Weaves.class, Weave.class]
                ]
        )

        assertFiles(input, activityClass)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)

        where:
        [activityClassName, activityClass] << MockClass.activityClasses
    }

    @Unroll
    def "generate _Helper if 2 @Extra are in multiple inner activity classes (where #activityClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyActivity1 extends #A {

                        @#E String mText;
                        @#E int mNumber;
                    }

                    public static class MyActivity2 extends #A {

                        @#E String mText;
                        @#E int mNumber;
                    }
                }
                """,
                [
                        E: Extra.class,
                        A: activityClass
                ]
        )

        expect:
        final JavaFileObject expected1 = JavaFile.newGeneratedFile("com.example", "Wrapper_MyActivity1_Helper",
                """
                abstract class #T {

                    @Weaves({
                        @Weave(
                            into="0^onCreate",
                            args = {"android.os.Bundle"},
                            statement = "com.example.#T.inject(this, this.getIntent());"
                        ),
                        @Weave(
                            into="0^onNewIntent",
                            args = {"android.content.Intent"},
                            statement = "com.example.#T.inject(this, \$1);"
                        )
                    })
                    public static void inject(#I.MyActivity1 target, Intent intent) {
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mText = extras.get("<Extra-mText>", target.mText);
                        target.mNumber = extras.get("<Extra-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Weaves.class, Weave.class]
                ]
        )
        final JavaFileObject expected2 = JavaFile.newGeneratedFile("com.example", "Wrapper_MyActivity2_Helper",
                """
                abstract class #T {

                    @Weaves({
                        @Weave(
                            into="0^onCreate",
                            args = {"android.os.Bundle"},
                            statement = "com.example.#T.inject(this, this.getIntent());"
                        ),
                        @Weave(
                            into="0^onNewIntent",
                            args = {"android.content.Intent"},
                            statement = "com.example.#T.inject(this, \$1);"
                        )
                    })
                    public static void inject(#I.MyActivity2 target, Intent intent) {
                        if (intent == null || intent.getExtras() == null) {
                            return;
                        }
                        BundleWrapper extras = BundleWrapper.from(intent.getExtras());
                        target.mText = extras.get("<Extra-mText>", target.mText);
                        target.mNumber = extras.get("<Extra-mNumber>", target.mNumber);
                    }
                }
                """,
                [
                        I: input,
                        _: [BundleWrapper.class, Intent.class, Weaves.class, Weave.class]
                ]
        )

        assertFiles(input, activityClass)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected1, expected2)

        where:
        [activityClassName, activityClass] << MockClass.activityClasses
    }
}