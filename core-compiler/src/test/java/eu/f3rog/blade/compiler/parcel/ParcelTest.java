package eu.f3rog.blade.compiler.parcel;

import android.os.Bundle;
import android.os.Parcelable;

import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaFileObject;

import blade.Parcel;
import eu.f3rog.blade.compiler.BaseTest;
import eu.f3rog.blade.compiler.BladeProcessor;
import eu.f3rog.blade.core.Weave;

import static eu.f3rog.blade.compiler.util.File.file;
import static eu.f3rog.blade.compiler.util.File.generatedFile;

/**
 * Class {@link ParcelTest}
 *
 * @author FrantisekGazo
 * @version 2016-01-21
 */
public class ParcelTest extends BaseTest {

    @Test
    public void withoutInterface() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P"
                )
                .body(
                        "@$P",
                        "public class $T {",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .failsToCompile()
                .withErrorContaining(ParcelErrorMsg.Invalid_Parcel_class);
    }

    @Test
    public void withoutInterfaceImplementation() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class
                )
                .body(
                        "@$P",
                        "public class $T implements Parcelable {",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .failsToCompile();
    }

    @Test
    public void withoutConstructor() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class
                )
                .body(
                        "@$P",
                        "public class $T implements Parcelable {",
                        "",
                        "   @Override",
                        "   public int describeContents() { return 0; }",
                        "   @Override",
                        "   public void writeToParcel(android.os.Parcel dest, int flags) {}",
                        "",
                        "}"
                );

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .failsToCompile()
                .withErrorContaining(ParcelErrorMsg.Parcel_class_without_constructor);
    }

    @Test
    public void validUsage() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class
                )
                .body(
                        "@$P",
                        "public class $T implements Parcelable {",
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

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError();
    }

    @Test
    public void fullTest() {
        JavaFileObject o = file("com.example", "MyObject")
                .imports(
                )
                .body(
                        "public class $T {",
                        "}"
                );
        JavaFileObject s = file("com.example", "DataS")
                .imports(
                        Serializable.class
                )
                .body(
                        "public class $T implements Serializable {",
                        "}"
                );
        JavaFileObject p = file("com.example", "DataP")
                .imports(
                        Parcelable.class
                )
                .body(
                        "public class $T implements Parcelable {",
                        "",
                        "   public static final Parcelable.Creator<$T> CREATOR = null;",
                        "",
                        "   @Override",
                        "   public int describeContents() { return 0; }",
                        "   @Override",
                        "   public void writeToParcel(android.os.Parcel dest, int flags) {}",
                        "",
                        "}"
                );

        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        o, "O",
                        s, "DS",
                        p, "DP",
                        Parcel.class, "P",
                        Parcelable.class,
                        Bundle.class
                )
                .body(
                        "@$P",
                        "public class $T implements Parcelable {",
                        "",
                        "   byte b;",
                        "   boolean flag;",
                        "   int numI;",
                        "   long numL;",
                        "   double numD;",
                        "   float numF;",
                        "   String text;",
                        "   Bundle bundle;",
                        "   Object obj;",
                        "   $O myObj;",
                        "   $DS dataS;",
                        "   $DP dataP;",
                        "   byte[] byteArray;",
                        "   boolean[] booleanArray;",
                        "   int[] intArray;",
                        "   long[] longArray;",
                        "   float[] floatArray;",
                        "   double[] doubleArray;",
                        "   char[] charArray;",
                        "   String[] stringArray;",
                        "   Object[] objectArray;",
                        "   $O[] arrayO;",
                        "   $DS[] arrayDS;",
                        "   $DP[] arrayDP;",
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
                        o, "O",
                        s, "DS",
                        p, "DP",
                        android.os.Parcel.class,
                        Parcelable.class,
                        Weave.class,
                        Object.class,
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
                        "   public static void writeToParcel($I target, Parcel parcel) {",
                        "       parcel.writeByte(target.b);",
                        "       parcel.writeByte((byte) (target.flag ? 1 : 0));",
                        "       parcel.writeInt(target.numI);",
                        "       parcel.writeLong(target.numL);",
                        "       parcel.writeDouble(target.numD);",
                        "       parcel.writeFloat(target.numF);",
                        "       parcel.writeString(target.text);",
                        "       parcel.writeBundle(target.bundle);",
                        "       parcel.writeValue(target.obj);",
                        "       parcel.writeValue(target.myObj);",
                        "       parcel.writeSerializable(target.dataS);",
                        "       parcel.writeParcelable(target.dataP, 0);",
                        "       parcel.writeByteArray(target.byteArray);",
                        "       parcel.writeBooleanArray(target.booleanArray);",
                        "       parcel.writeIntArray(target.intArray);",
                        "       parcel.writeLongArray(target.longArray);",
                        "       parcel.writeFloatArray(target.floatArray);",
                        "       parcel.writeDoubleArray(target.doubleArray);",
                        "       parcel.writeCharArray(target.charArray);",
                        "       parcel.writeStringArray(target.stringArray);",
                        "       parcel.writeArray(target.objectArray);",
                        "       parcel.writeArray(target.arrayO);",
                        "       parcel.writeArray(target.arrayDS);",
                        "       parcel.writeTypedArray(target.arrayDP, 0);",
                        "   }",
                        "",
                        "   @Weave(into = \"\", args = {\"android.os.Parcel\"}, statement = \"com.example.$T.readFromParcel(this, $1);\")",
                        "   public static void readFromParcel($I target, Parcel parcel) {",
                        "       target.b = parcel.readByte();",
                        "       target.flag = parcel.readByte() > 0;",
                        "       target.numI = parcel.readInt();",
                        "       target.numL = parcel.readLong();",
                        "       target.numD = parcel.readDouble();",
                        "       target.numF = parcel.readFloat();",
                        "       target.text = parcel.readString();",
                        "       target.bundle = parcel.readBundle();",
                        "       target.obj = (Object) parcel.readValue(Object.class.getClassLoader());",
                        "       target.myObj = ($O) parcel.readValue($O.class.getClassLoader());",
                        "       target.dataS = ($DS) parcel.readSerializable();",
                        "       target.dataP = ($DP) parcel.readParcelable($DP.class.getClassLoader());",
                        "       target.byteArray = parcel.createByteArray();",
                        "       target.booleanArray = parcel.createBooleanArray();",
                        "       target.intArray = parcel.createIntArray();",
                        "       target.longArray = parcel.createLongArray();",
                        "       target.floatArray = parcel.createFloatArray();",
                        "       target.doubleArray = parcel.createDoubleArray();",
                        "       target.charArray = parcel.createCharArray();",
                        "       target.stringArray = parcel.createStringArray();",
                        "       target.objectArray = (Object[]) parcel.readArray(Object[].class.getClassLoader());",
                        "       target.arrayO = ($O[]) parcel.readArray($O[].class.getClassLoader());",
                        "       target.arrayDS = ($DS[]) parcel.readArray($DS[].class.getClassLoader());",
                        "       target.arrayDP = parcel.createTypedArray($DP.CREATOR);",
                        "   }",
                        "",
                        "}"
                );

        assertFiles(o, s, p, input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected);


    }

    @Test
    public void ignoreCollections() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class,
                        List.class,
                        ArrayList.class,
                        LinkedList.class,
                        Set.class,
                        HashSet.class,
                        Map.class,
                        HashMap.class
                )
                .body(
                        "@$P",
                        "public class $T implements Parcelable {",
                        "",// only field should processed
                        "   String text;",
                        "",// this fields should be ignored
                        "   List<String> list1;",
                        "   ArrayList<String> list2;",
                        "   LinkedList<Object> list3;",
                        "   Set<String> set1;",
                        "   HashSet<Long> set2;",
                        "   Map<Long, String> map1;",
                        "   HashMap<Long, String> map2;",
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
                        "   public static void writeToParcel($I target, Parcel parcel) {",
                        "       parcel.writeString(target.text);",
                        "   }",
                        "",
                        "   @Weave(into = \"\", args = {\"android.os.Parcel\"}, statement = \"com.example.$T.readFromParcel(this, $1);\")",
                        "   public static void readFromParcel($I target, Parcel parcel) {",
                        "       target.text = parcel.readString();",
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
    public void ignorePrivateFields() {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class,
                        List.class
                )
                .body(
                        "@$P",
                        "public class $T implements Parcelable {",
                        "",// only field should processed
                        "   String text;",
                        "",// this fields should be ignored
                        "   private String p1;",
                        "   protected String p2;",
                        "   static String p3;",
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
                        "   public static void writeToParcel($I target, Parcel parcel) {",
                        "       parcel.writeString(target.text);",
                        "   }",
                        "",
                        "   @Weave(into = \"\", args = {\"android.os.Parcel\"}, statement = \"com.example.$T.readFromParcel(this, $1);\")",
                        "   public static void readFromParcel($I target, Parcel parcel) {",
                        "       target.text = parcel.readString();",
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
    public void validParcel() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        JavaFileObject input = file("com.example", "MyClass")
                .imports(
                        Parcel.class, "P",
                        Parcelable.class
                )
                .body(
                        "@$P",
                        "public class $T implements Parcelable {",
                        "",
                        "   String text;",
                        "   boolean flag;",
                        "   int[] array;",
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
                        "   public static void writeToParcel($I target, Parcel parcel) {",
                        "       parcel.writeString(target.text);",
                        "       parcel.writeByte((byte) (target.flag ? 1 : 0));",
                        "       parcel.writeIntArray(target.array);",
                        "   }",
                        "",
                        "   @Weave(into = \"\", args = {\"android.os.Parcel\"}, statement = \"com.example.$T.readFromParcel(this, $1);\")",
                        "   public static void readFromParcel($I target, Parcel parcel) {",
                        "       target.text = parcel.readString();",
                        "       target.flag = parcel.readByte() > 0;",
                        "       target.array = parcel.createIntArray();",
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