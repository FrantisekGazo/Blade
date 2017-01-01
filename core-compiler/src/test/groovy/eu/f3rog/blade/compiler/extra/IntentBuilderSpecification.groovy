package eu.f3rog.blade.compiler.extra

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import blade.Blade
import blade.Extra
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.BundleWrapper
import blade.Bundler
import eu.f3rog.blade.core.GeneratedFor

import javax.tools.JavaFileObject

public final class IntentBuilderSpecification
        extends BaseSpecification {

    def "generate for an Activity with only @Blade"() {
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

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for an Activity with 1 @Extra"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyActivity",
                """
                public class #T extends Activity {

                    @#E String mText;
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

        assertFiles(input)
                .with(BladeProcessor.Module.EXTRA)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate for 2 Activities with @Extra"() {
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

    def "generate for 2 Activities with inherited @Extra"() {
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

    def "generate for 1 Activity with inherited @Extra from abstract class"() {
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

    def "generate for an Activity with @Extra and custom Bundler"() {
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
}
