package eu.f3rog.blade.compiler.parcel;

import android.os.Parcelable;

import org.junit.Test;

import java.io.Serializable;

import javax.tools.JavaFileObject;

import blade.Parcel;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.Weave;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link GenericClassTest}
 *
 * @author FrantisekGazo
 * @version 2016-01-21
 */
public class GenericClassTest extends BaseTest {

    @Test
    public void fieldWithGeneric() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        JavaFileObject a = file("com.example", "A")
                .imports(
                        Serializable.class
                )
                .body(
                        "public class $T<T> {",
                        "",
                        "}"
                );
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class
                )
                .body(
                        "@$P",
                        "public class $T<T extends String, S extends Long> implements Parcelable {",
                        "",
                        "   A<Long> a1;",
                        "   A a2;",
                        "   A<T> a3;",
                        "",
                        "   public $T(android.os.Parcel p) {}",
                        "",
                        "   @Override",
                        "   public int describeContents() { return 0; }",
                        "   @Override",
                        "   public void writeToParcel(android.os.Parcel dest, int flags) {}",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyClass_Helper")
                .imports(
                        input, "I",
                        android.os.Parcel.class,
                        Parcelable.class,
                        Weave.class,
                        Override.class,
                        String.class,
                        Long.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into = \"<FIELD>\", statement = \"com.example.$T.CREATOR\")",
                        "   public static final Parcelable.Creator<$I> CREATOR = ",
                        "     new Parcelable.Creator<$I>() {",
                        "       @Override",
                        "       public $I createFromParcel(Parcel in) {",
                        "           return new $I(in);",
                        "       }",
                        "",
                        "       @Override",
                        "       public $I[] newArray(int size) {",
                        "           return new $I[size];",
                        "       }",
                        "   };",
                        "",
                        "   @Weave(into = \">writeToParcel\", args = {\"android.os.Parcel\", \"int\"}, statement = \"com.example.$T.writeToParcel(this, $1);\")",
                        "   public static <T extends String, S extends Long> void writeToParcel($I<T, S> target, Parcel parcel) {",
                        "       parcel.writeValue(target.a1);",
                        "       parcel.writeValue(target.a2);",
                        "       parcel.writeValue(target.a3);",
                        "   }",
                        "",
                        "   @Weave(into = \"\", args = {\"android.os.Parcel\"}, statement = \"com.example.$T.readFromParcel(this, $1);\")",
                        "   public static <T extends String, S extends Long> void readFromParcel($I<T, S> target, Parcel parcel) {",
                        "       target.a1 = (A<Long>) parcel.readValue(A.class.getClassLoader());",
                        "       target.a2 = (A) parcel.readValue(A.class.getClassLoader());",
                        "       target.a3 = (A<T>) parcel.readValue(A.class.getClassLoader());",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(a, input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void genericField() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class
                )
                .body(
                        "@$P",
                        "public class $T<T> implements Parcelable {",
                        "",
                        "   T data;",
                        "",
                        "   public $T(android.os.Parcel p) {}",
                        "",
                        "   @Override",
                        "   public int describeContents() { return 0; }",
                        "   @Override",
                        "   public void writeToParcel(android.os.Parcel dest, int flags) {}",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyClass_Helper")
                .imports(
                        input, "I",
                        android.os.Parcel.class,
                        Parcelable.class,
                        Weave.class,
                        Override.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into = \"<FIELD>\", statement = \"com.example.$T.CREATOR\")",
                        "   public static final Parcelable.Creator<$I> CREATOR = ",
                        "     new Parcelable.Creator<$I>() {",
                        "       @Override",
                        "       public $I createFromParcel(Parcel in) {",
                        "           return new $I(in);",
                        "       }",
                        "",
                        "       @Override",
                        "       public $I[] newArray(int size) {",
                        "           return new $I[size];",
                        "       }",
                        "   };",
                        "",
                        "   @Weave(into = \">writeToParcel\", args = {\"android.os.Parcel\", \"int\"}, statement = \"com.example.$T.writeToParcel(this, $1);\")",
                        "   public static <T> void writeToParcel($I<T> target, Parcel parcel) {",
                        "       parcel.writeValue(target.data);",
                        "   }",
                        "",
                        "   @Weave(into = \"\", args = {\"android.os.Parcel\"}, statement = \"com.example.$T.readFromParcel(this, $1);\")",
                        "   public static <T> void readFromParcel($I<T> target, Parcel parcel) {",
                        "       target.data = (T) parcel.readValue(null);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test
    public void genericFieldExtends() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class
                )
                .body(
                        "@$P",
                        "public class $T<T extends String> implements Parcelable {",
                        "",
                        "   T data;",
                        "",
                        "   public $T(android.os.Parcel p) {}",
                        "",
                        "   @Override",
                        "   public int describeContents() { return 0; }",
                        "   @Override",
                        "   public void writeToParcel(android.os.Parcel dest, int flags) {}",
                        "",
                        "}"
                );

        JavaFileObject expected = generatedFile("com.example", "MyClass_Helper")
                .imports(
                        input, "I",
                        android.os.Parcel.class,
                        Parcelable.class,
                        Weave.class,
                        Override.class,
                        String.class
                )
                .body(
                        "abstract class $T {",
                        "",
                        "   @Weave(into = \"<FIELD>\", statement = \"com.example.$T.CREATOR\")",
                        "   public static final Parcelable.Creator<$I> CREATOR = ",
                        "     new Parcelable.Creator<$I>() {",
                        "       @Override",
                        "       public $I createFromParcel(Parcel in) {",
                        "           return new $I(in);",
                        "       }",
                        "",
                        "       @Override",
                        "       public $I[] newArray(int size) {",
                        "           return new $I[size];",
                        "       }",
                        "   };",
                        "",
                        "   @Weave(into = \">writeToParcel\", args = {\"android.os.Parcel\", \"int\"}, statement = \"com.example.$T.writeToParcel(this, $1);\")",
                        "   public static <T extends String> void writeToParcel($I<T> target, Parcel parcel) {",
                        "       parcel.writeValue(target.data);",
                        "   }",
                        "",
                        "   @Weave(into = \"\", args = {\"android.os.Parcel\"}, statement = \"com.example.$T.readFromParcel(this, $1);\")",
                        "   public static <T extends String> void readFromParcel($I<T> target, Parcel parcel) {",
                        "       target.data = (T) parcel.readValue(String.class.getClassLoader());",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}
