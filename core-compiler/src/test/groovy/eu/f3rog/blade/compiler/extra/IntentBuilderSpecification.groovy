package eu.f3rog.blade.compiler.extra

import android.app.Activity
import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import blade.Blade
import blade.Bundler
import blade.Extra
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.MockClass
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.BundleWrapper
import eu.f3rog.blade.core.GeneratedFor
import spock.lang.Unroll

import javax.tools.JavaFileObject

public final class IntentBuilderSpecification
        extends BaseSpecification {

    @Unroll
    def "do not generate if no activity class need it (where #activityClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends #A {}
                """,
                [
                        A: activityClass
                ]
        )

        expect:
        compilesWithoutErrorAndDoesntGenerate("blade", "I", BladeProcessor.Module.EXTRA, input, activityClass)

        where:
        [activityClassName, activityClass] << MockClass.activityClasses
    }

    @Unroll
    def "generate for an activity with only @Blade (where #activityClassName)"() {
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
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                @#GF(#A.class)
                   public static Intent for#A(Context context) {
                       Intent intent = new Intent(context, #A.class);
                       #BW extras = new #BW();
                       intent.putExtras(extras.getBundle());
                       return intent;
                   }

                   @#GF(#A.class)
                   public static void start#A(Context context) {
                       context.startActivity(for#A(context));
                   }

                }
                """,
                [
                        A : input,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class]
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
    def "generate for an activity class with 1 @Extra #type"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#E $type mField;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#A.class)
                    public static Intent for#A(Context context, $type mField) {
                        Intent intent = new Intent(context, #A.class);
                        #BW extras = new #BW();
                        extras.put("<Extra-mField>", mField);
                        intent.putExtras(extras.getBundle());
                        return intent;
                    }

                    @#GF(#A.class)
                    public static void start#A(Context context, $type mField) {
                        context.startActivity(for#A(context, mField));
                    }
                }
                """,
                [
                        A : input,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class]
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
    }

    @Unroll
    def "generate for an activity class with 1 @Extra String (where #activityClassName)"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends #A {

                    @#E String mText;
                }
                """,
                [
                        E: Extra.class,
                        A: activityClass
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#A.class)
                    public static Intent for#A(Context context, String mText) {
                        Intent intent = new Intent(context, #A.class);
                        #BW extras = new #BW();
                        extras.put("<Extra-mText>", mText);
                        intent.putExtras(extras.getBundle());
                        return intent;
                    }

                    @#GF(#A.class)
                    public static void start#A(Context context, String mText) {
                        context.startActivity(for#A(context, mText));
                    }
                }
                """,
                [
                        A : input,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class, String.class]
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

    def "generate for 2 activity classes with @Extra"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "FirstActivity",
                """
                public class #T extends Activity {

                    @#E int number;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )
        final JavaFileObject input2 = JavaFile.newFile("com.example", "SecondActivity",
                """
                public class #T extends Activity {

                    @#E String text;
                    @#E boolean flag;
                    @#E double number;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#A1.class)
                    public static Intent for#A1(Context context, int number) {
                       Intent intent = new Intent(context, #A1.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-number>", number);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A1.class)
                    public static void start#A1(Context context, int number) {
                       context.startActivity(for#A1(context, number));
                    }

                    @#GF(#A2.class)
                    public static Intent for#A2(Context context, String text, boolean flag, double number) {
                       Intent intent = new Intent(context, #A2.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-text>", text);
                       extras.put("<Extra-flag>", flag);
                       extras.put("<Extra-number>", number);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A2.class)
                    public static void start#A2(Context context, String text, boolean flag, double number) {
                       context.startActivity(for#A2(context, text, flag, number));
                    }
                """,
                [
                        A1: input1,
                        A2: input2,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class, String.class]
                ]
        )

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for 2 activity classes with inherited @Extra"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "BaseActivity",
                """
                public class #T extends Activity {

                    @#E int number;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )
        final JavaFileObject input2 = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends #B {

                    @#E String text;
                }
                """,
                [
                        E: Extra.class,
                        B: input1
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#A1.class)
                    public static Intent for#A1(Context context, int number) {
                       Intent intent = new Intent(context, #A1.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-number>", number);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A1.class)
                    public static void start#A1(Context context, int number) {
                       context.startActivity(for#A1(context, number));
                    }

                    @#GF(#A2.class)
                    public static Intent for#A2(Context context, int number, String text) {
                       Intent intent = new Intent(context, #A2.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-number>", number);
                       extras.put("<Extra-text>", text);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A2.class)
                    public static void start#A2(Context context, int number, String text) {
                       context.startActivity(for#A2(context, number, text));
                    }
                """,
                [
                        A1: input1,
                        A2: input2,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class, String.class]
                ]
        )

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for 1 activity class with inherited @Extra from an abstract class"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "BaseActivity",
                """
                public abstract class #T extends Activity {

                    @#E int number;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )
        final JavaFileObject input2 = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends #B {

                    @#E String text;
                }
                """,
                [
                        E: Extra.class,
                        B: input1
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#A2.class)
                    public static Intent for#A2(Context context, int number, String text) {
                       Intent intent = new Intent(context, #A2.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-number>", number);
                       extras.put("<Extra-text>", text);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A2.class)
                    public static void start#A2(Context context, int number, String text) {
                       context.startActivity(for#A2(context, number, text));
                    }
                """,
                [
                        A2: input2,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class, String.class]
                ]
        )

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for an activity class with @Extra and custom Bundler"() {
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
                    @#E int mNumber;
                }
                """,
                [
                        E : Extra.class,
                        CB: customBundler,
                        _ : [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                @#GF(#A.class)
                   public static Intent for#A(Context context, String mText, int mNumber) {
                       Intent intent = new Intent(context, #A.class);
                       #BW extras = new #BW();

                       Bundle mTextBundle = new Bundle();
                       #CB mTextBundler = new #CB();
                       mTextBundler.save(mText, mTextBundle);
                       extras.put("<Extra-mText>", mTextBundle);

                       extras.put("<Extra-mNumber>", mNumber);
                       intent.putExtras(extras.getBundle());
                       return intent;
                   }

                   @#GF(#A.class)
                   public static void start#A(Context context, String mText, int mNumber) {
                       context.startActivity(for#A(context, mText, mNumber));
                   }

                }
                """,
                [
                        A : input,
                        BW: BundleWrapper.class,
                        CB: customBundler,
                        GF: GeneratedFor.class,
                        _ : [Bundle.class, Intent.class, Context.class, String.class]
                ]
        )

        assertFiles(customBundler, input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a generic activity class <T> with 2 @Extra"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T<T> extends Activity {

                    @#E String mText;
                    @#E int mNumber;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#I.class)
                    public static Intent for#I(Context context, String mText, int mNumber) {
                        Intent intent = new Intent(context, #I.class);
                        #BW extras = new #BW();
                        extras.put("<Extra-mText>", mText);
                        extras.put("<Extra-mNumber>", mNumber);
                        intent.putExtras(extras.getBundle());
                        return intent;
                    }

                    @#GF(#I.class)
                    public static void start#I(Context context, String mText, int mNumber) {
                        context.startActivity(for#I(context, mText, mNumber));
                    }
                }
                """,
                [
                        I : input,
                        GF: GeneratedFor.class,
                        BW: BundleWrapper.class,
                        _ : [Context.class, Intent.class, String.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for a generic activity class <T extends Serializable> with 2 @Extra"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T<T extends Serializable> extends Activity {

                    @#E T mData;
                    @#E int mNumber;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class, Serializable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#I.class)
                    public static Intent for#I(Context context, Serializable mData, int mNumber) {
                        Intent intent = new Intent(context, #I.class);
                        #BW extras = new #BW();
                        extras.put("<Extra-mData>", mData);
                        extras.put("<Extra-mNumber>", mNumber);
                        intent.putExtras(extras.getBundle());
                        return intent;
                    }

                    @#GF(#I.class)
                    public static void start#I(Context context, Serializable mData, int mNumber) {
                        context.startActivity(for#I(context, mData, mNumber));
                    }
                }
                """,
                [
                        I : input,
                        GF: GeneratedFor.class,
                        BW: BundleWrapper.class,
                        _ : [Context.class, Intent.class, Serializable.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for an inner activity class"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyActivity extends Activity {

                        @#E String mText;
                        @#E int mNumber;
                    }
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#I.MyActivity.class)
                    public static Intent for#IMyActivity(Context context, String mText, int mNumber) {
                        Intent intent = new Intent(context, #I.MyActivity.class);
                        #BW extras = new #BW();
                        extras.put("<Extra-mText>", mText);
                        extras.put("<Extra-mNumber>", mNumber);
                        intent.putExtras(extras.getBundle());
                        return intent;
                    }

                    @#GF(#I.MyActivity.class)
                    public static void start#IMyActivity(Context context, String mText, int mNumber) {
                        context.startActivity(for#IMyActivity(context, mText, mNumber));
                    }
                }
                """,
                [
                        I : input,
                        GF: GeneratedFor.class,
                        BW: BundleWrapper.class,
                        _ : [Context.class, Intent.class, String.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for 2 inner activity classes"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "Wrapper",
                """
                public class #T {

                    public static class MyActivity1 extends Activity {

                        @#E String mText;
                        @#E int mNumber;
                    }

                    public static class MyActivity2 extends Activity {

                        @#E String mText;
                        @#E int mNumber;
                    }
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#I.MyActivity1.class)
                    public static Intent for#IMyActivity1(Context context, String mText, int mNumber) {
                        Intent intent = new Intent(context, #I.MyActivity1.class);
                        #BW extras = new #BW();
                        extras.put("<Extra-mText>", mText);
                        extras.put("<Extra-mNumber>", mNumber);
                        intent.putExtras(extras.getBundle());
                        return intent;
                    }

                    @#GF(#I.MyActivity1.class)
                    public static void start#IMyActivity1(Context context, String mText, int mNumber) {
                        context.startActivity(for#IMyActivity1(context, mText, mNumber));
                    }

                    @#GF(#I.MyActivity2.class)
                    public static Intent for#IMyActivity2(Context context, String mText, int mNumber) {
                        Intent intent = new Intent(context, #I.MyActivity2.class);
                        #BW extras = new #BW();
                        extras.put("<Extra-mText>", mText);
                        extras.put("<Extra-mNumber>", mNumber);
                        intent.putExtras(extras.getBundle());
                        return intent;
                    }

                    @#GF(#I.MyActivity2.class)
                    public static void start#IMyActivity2(Context context, String mText, int mNumber) {
                        context.startActivity(for#IMyActivity2(context, mText, mNumber));
                    }
                }
                """,
                [
                        I : input,
                        GF: GeneratedFor.class,
                        BW: BundleWrapper.class,
                        _ : [Context.class, Intent.class, String.class]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for different activity classes with @Extra"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "FirstActivity",
                """
                public class #T extends Activity {

                    @#E int number;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )
        final JavaFileObject input2 = JavaFile.newFile("com.example", "SecondActivity",
                """
                public class #T extends #A {

                    @#E boolean flag;
                }
                """,
                [
                        E: Extra.class,
                        A: MockClass.SUPPORT_ACTIVITY
                ]
        )
        final JavaFileObject input3 = JavaFile.newFile("com.example", "ThirdActivity",
                """
                public class #T extends #A {

                    @#E String text;
                }
                """,
                [
                        E: Extra.class,
                        A: MockClass.ANDROIDX_ACTIVITY
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#A1.class)
                    public static Intent for#A1(Context context, int number) {
                       Intent intent = new Intent(context, #A1.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-number>", number);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A1.class)
                    public static void start#A1(Context context, int number) {
                       context.startActivity(for#A1(context, number));
                    }

                    @#GF(#A2.class)
                    public static Intent for#A2(Context context, boolean flag) {
                       Intent intent = new Intent(context, #A2.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-flag>", flag);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A2.class)
                    public static void start#A2(Context context, boolean flag) {
                       context.startActivity(for#A2(context, flag));
                    }

                    @#GF(#A3.class)
                    public static Intent for#A3(Context context, String text) {
                       Intent intent = new Intent(context, #A3.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-text>", text);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A3.class)
                    public static void start#A3(Context context, String text) {
                       context.startActivity(for#A3(context, text));
                    }
                """,
                [
                        A1: input1,
                        A2: input2,
                        A3: input3,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class, String.class]
                ]
        )

        assertFiles(MockClass.SUPPORT_ACTIVITY, MockClass.ANDROIDX_ACTIVITY, input1, input2, input3)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for different service classes with @Extra"() {
        given:
        final JavaFileObject input1 = JavaFile.newFile("com.example", "FirstService",
                """
                public class #T extends Service {

                    @#E String text;

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
        final JavaFileObject input2 = JavaFile.newFile("com.example", "SecondService",
                """
                public class #T extends IntentService {

                    @#E int number;

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
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#A1.class)
                    public static Intent for#A1(Context context, String text) {
                       Intent intent = new Intent(context, #A1.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-text>", text);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A1.class)
                    public static void start#A1(Context context, String text) {
                       context.startService(for#A1(context, text));
                    }

                    @#GF(#A2.class)
                    public static Intent for#A2(Context context, int number) {
                       Intent intent = new Intent(context, #A2.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-number>", number);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A2.class)
                    public static void start#A2(Context context, int number) {
                       context.startService(for#A2(context, number));
                    }
                """,
                [
                        A1: input1,
                        A2: input2,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class, String.class]
                ]
        )

        assertFiles(input1, input2)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for activity and service classes with @Extra"() {
        given:
        final JavaFileObject activity = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#E int number;
                }
                """,
                [
                        E: Extra.class,
                        _: [Activity.class]
                ]
        )
        final JavaFileObject service = JavaFile.newFile("com.example", "MyService",
                """
                public class #T extends Service {

                    @#E String text;

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
        final JavaFileObject expected = JavaFile.newGeneratedFile("blade", "I",
                """
                public class #T {

                    @#GF(#A1.class)
                    public static Intent for#A1(Context context, int number) {
                       Intent intent = new Intent(context, #A1.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-number>", number);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#A1.class)
                    public static void start#A1(Context context, int number) {
                       context.startActivity(for#A1(context, number));
                    }

                    @#GF(#S1.class)
                    public static Intent for#S1(Context context, String text) {
                       Intent intent = new Intent(context, #S1.class);
                       #BW extras = new #BW();
                       extras.put("<Extra-text>", text);
                       intent.putExtras(extras.getBundle());
                       return intent;
                    }

                    @#GF(#S1.class)
                    public static void start#S1(Context context, String text) {
                       context.startService(for#S1(context, text));
                    }
                """,
                [
                        A1: activity,
                        S1: service,
                        BW: BundleWrapper.class,
                        GF: GeneratedFor.class,
                        _ : [Intent.class, Context.class, String.class]
                ]
        )

        assertFiles(activity, service)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }
}
