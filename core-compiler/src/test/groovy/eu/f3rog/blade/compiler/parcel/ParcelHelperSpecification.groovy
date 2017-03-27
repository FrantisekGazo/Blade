package eu.f3rog.blade.compiler.parcel

import android.os.Bundle
import android.os.Parcelable
import blade.Parcel
import blade.ParcelIgnore
import eu.f3rog.blade.compiler.BaseSpecification
import eu.f3rog.blade.compiler.BladeProcessor
import eu.f3rog.blade.compiler.util.JavaFile
import eu.f3rog.blade.core.Weave
import eu.f3rog.blade.core.WeaveInto

import javax.tools.JavaFileObject

public final class ParcelHelperSpecification
        extends BaseSpecification {

    def "fail if class is not Parcelable"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T {

                }
                """,
                [
                        P: Parcel.class
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .failsToCompile()
                .withErrorContaining(ParcelErrorMsg.Invalid_Parcel_class)
    }

    def "fail if class does not implement Parcelable"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .failsToCompile()
    }

    def "fail if class does not have constructor for Parcelable"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .failsToCompile()
                .withErrorContaining(ParcelErrorMsg.Parcel_class_without_constructor)
    }

    def "generate _Helper for an empty class"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
    }

    def "generate _Helper for a class with fields"() {
        given:
        final JavaFileObject o = JavaFile.newFile("com.example", "MyObject",
                """
                public class #T {
                }
                """,
                [
                        _: []
                ]
        )
        final JavaFileObject s = JavaFile.newFile("com.example", "DataS",
                """
                public class #T implements Serializable {
                }
                """,
                [
                        _: [Serializable.class]
                ]
        )
        final JavaFileObject p = JavaFile.newFile("com.example", "DataP",
                """
                public class #T implements Parcelable {

                    public static final Parcelable.Creator<#T> CREATOR = null;

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        _: [Parcelable.class]
                ]
        )
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    byte b;
                    boolean flag;
                    int numI;
                    long numL;
                    double numD;
                    float numF;
                    String text;
                    Bundle bundle;
                    Object obj;
                    #O myObj;
                    #DS dataS;
                    #DP dataP;
                    byte[] byteArray;
                    boolean[] booleanArray;
                    int[] intArray;
                    long[] longArray;
                    float[] floatArray;
                    double[] doubleArray;
                    char[] charArray;
                    String[] stringArray;
                    Object[] objectArray;
                    #O[] arrayO;
                    #DS[] arrayDS;
                    #DP[] arrayDP;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        O: o,
                        DS: s,
                        DP: p,
                        P: Parcel.class,
                        _: [Parcelable.class, Bundle.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR = 
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }                    
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static void writeToParcel(#I target, Parcel parcel) {
                        parcel.writeByte(target.b);
                        parcel.writeByte((byte) (target.flag ? 1 : 0));
                        parcel.writeInt(target.numI);
                        parcel.writeLong(target.numL);
                        parcel.writeDouble(target.numD);
                        parcel.writeFloat(target.numF);
                        parcel.writeString(target.text);
                        parcel.writeBundle(target.bundle);
                        parcel.writeValue(target.obj);
                        parcel.writeValue(target.myObj);
                        parcel.writeSerializable(target.dataS);
                        parcel.writeParcelable(target.dataP, 0);
                        parcel.writeByteArray(target.byteArray);
                        parcel.writeBooleanArray(target.booleanArray);
                        parcel.writeIntArray(target.intArray);
                        parcel.writeLongArray(target.longArray);
                        parcel.writeFloatArray(target.floatArray);
                        parcel.writeDoubleArray(target.doubleArray);
                        parcel.writeCharArray(target.charArray);
                        parcel.writeStringArray(target.stringArray);
                        parcel.writeArray(target.objectArray);
                        parcel.writeArray(target.arrayO);
                        parcel.writeArray(target.arrayDS);
                        parcel.writeTypedArray(target.arrayDP, 0);
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static void readFromParcel(#I target, Parcel parcel) {
                        target.b = parcel.readByte();
                        target.flag = parcel.readByte() > 0;
                        target.numI = parcel.readInt();
                        target.numL = parcel.readLong();
                        target.numD = parcel.readDouble();
                        target.numF = parcel.readFloat();
                        target.text = parcel.readString();
                        target.bundle = parcel.readBundle();
                        target.obj = (Object) parcel.readValue(Object.class.getClassLoader());
                        target.myObj = (#O) parcel.readValue(#O.class.getClassLoader());
                        target.dataS = (#DS) parcel.readSerializable();
                        target.dataP = (#DP) parcel.readParcelable(#DP.class.getClassLoader());
                        target.byteArray = parcel.createByteArray();
                        target.booleanArray = parcel.createBooleanArray();
                        target.intArray = parcel.createIntArray();
                        target.longArray = parcel.createLongArray();
                        target.floatArray = parcel.createFloatArray();
                        target.doubleArray = parcel.createDoubleArray();
                        target.charArray = parcel.createCharArray();
                        target.stringArray = parcel.createStringArray();
                        target.objectArray = (Object[]) parcel.readArray(Object.class.getClassLoader());
                        target.arrayO = (#O[]) parcel.readArray(#O.class.getClassLoader());
                        target.arrayDS = (#DS[]) parcel.readArray(#DS.class.getClassLoader());
                        target.arrayDP = parcel.createTypedArray(#DP.CREATOR);
                    }
                }
                """,
                [
                        I: input,
                        O: o,
                        DS: s,
                        DP: p,
                        _: [
                                android.os.Parcel.class,
                                Parcelable.class,
                                Object.class,
                                Override.class,
                                Weave.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(o, s, p, input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a class with collection fields"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    String text;
                    List<String> list1;
                    ArrayList<String> list2;
                    LinkedList<Object> list3;
                    Set<String> set1;
                    HashSet<Long> set2;
                    Map<Long, String> map1;
                    HashMap<Long, String> map2;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [
                                Parcelable.class,
                                List.class,
                                ArrayList.class,
                                LinkedList.class,
                                Set.class,
                                HashSet.class,
                                Map.class,
                                HashMap.class
                        ]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR =
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static void writeToParcel(#I target, Parcel parcel) {
                        parcel.writeString(target.text);
                        parcel.writeValue(target.list1);
                        parcel.writeSerializable(target.list2);
                        parcel.writeSerializable(target.list3);
                        parcel.writeValue(target.set1);
                        parcel.writeSerializable(target.set2);
                        parcel.writeValue(target.map1);
                        parcel.writeSerializable(target.map2);
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static void readFromParcel(#I target, Parcel parcel) {
                        target.text = parcel.readString();
                        target.list1 = (List<String>) parcel.readValue(List.class.getClassLoader());
                        target.list2 = (ArrayList<String>) parcel.readSerializable();
                        target.list3 = (LinkedList<Object>) parcel.readSerializable();
                        target.set1 = (Set<String>) parcel.readValue(Set.class.getClassLoader());
                        target.set2 = (HashSet<Long>) parcel.readSerializable();
                        target.map1 = (Map<Long, String>) parcel.readValue(Map.class.getClassLoader());
                        target.map2 = (HashMap<Long, String>) parcel.readSerializable();
                    }
                }
                """,
                [
                        I: input,
                        _: [
                                android.os.Parcel.class,
                                Parcelable.class,
                                Weave.class,
                                Override.class,
                                Long.class,
                                Object.class,
                                String.class,
                                ArrayList.class,
                                LinkedList.class,
                                HashMap.class,
                                HashSet.class,
                                List.class,
                                Map.class,
                                Set.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a class with a static field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    String text;
                    static String p3;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR =
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static void writeToParcel(#I target, Parcel parcel) {
                        parcel.writeString(target.text);
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static void readFromParcel(#I target, Parcel parcel) {
                        target.text = parcel.readString();
                    }
                }
                """,
                [
                        I: input,
                        _: [
                                android.os.Parcel.class,
                                Parcelable.class,
                                Weave.class,
                                Override.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "fail if a getter is missing for a private field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    private String text;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .failsToCompile()
                .withErrorContaining(String.format(ParcelErrorMsg.Missing_Access_Method, "text", "getter"))
    }

    def "fail if a setter is missing for a private field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    private String text;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }

                    public String getText() {
                        return text;
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .failsToCompile()
                .withErrorContaining(String.format(ParcelErrorMsg.Missing_Access_Method, "text", "setter"))
    }

    def "generate _Helper for a class with a private field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    private String text1;
                    protected String text2;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }

                    public String getText1() {
                        return text1;
                    }

                    public void setText1(String newText1) {
                        text1 = newText1;
                    }

                    public String getText2() {
                        return text2;
                    }

                    public void setText2(String newText2) {
                        text2 = newText2;
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR =
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static void writeToParcel(#I target, Parcel parcel) {
                        parcel.writeString(target.getText1());
                        parcel.writeString(target.getText2());
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static void readFromParcel(#I target, Parcel parcel) {
                        target.setText1(parcel.readString());
                        target.setText2(parcel.readString());
                    }
                }
                """,
                [
                        I: input,
                        _: [
                                android.os.Parcel.class,
                                Parcelable.class,
                                Weave.class,
                                Override.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a class with a @ParcelIgnore field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    String text1;
                    @#PI
                    String text2;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        P: Parcel.class,
                        PI: ParcelIgnore.class.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR =
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static void writeToParcel(#I target, Parcel parcel) {
                        parcel.writeString(target.text1);
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static void readFromParcel(#I target, Parcel parcel) {
                        target.text1 = parcel.readString();
                    }
                }
                """,
                [
                        I: input,
                        _: [
                                android.os.Parcel.class,
                                Parcelable.class,
                                Weave.class,
                                Override.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a class with a @ParcelIgnore protected field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T implements Parcelable {

                    private String text1;
                    @#PI
                    protected String text2;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }

                    public String getText1() {
                        return text1;
                    }

                    public void setText1(String newText1) {
                        text1 = newText1;
                    }
                }
                """,
                [
                        P: Parcel.class,
                        PI: ParcelIgnore.class.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR =
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static void writeToParcel(#I target, Parcel parcel) {
                        parcel.writeString(target.getText1());
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static void readFromParcel(#I target, Parcel parcel) {
                        target.setText1(parcel.readString());
                    }
                }
                """,
                [
                        I: input,
                        _: [
                                android.os.Parcel.class,
                                Parcelable.class,
                                Weave.class,
                                Override.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a generic class"() {
        given:
        final JavaFileObject a = JavaFile.newFile("com.example", "A",
                """
                public class #T<T> {
                }
                """,
                [
                        _: []
                ]
        )
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T<T extends String, S extends Long> implements Parcelable {

                    A<Long> a1;
                    #A a2;
                    #A<T> a3;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        A: a,
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR =
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static <T extends String, S extends Long> void writeToParcel(#I<T, S> target, Parcel parcel) {
                        parcel.writeValue(target.a1);
                        parcel.writeValue(target.a2);
                        parcel.writeValue(target.a3);
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static <T extends String, S extends Long> void readFromParcel(#I<T, S> target, Parcel parcel) {
                        target.a1 = (A<Long>) parcel.readValue(A.class.getClassLoader());
                        target.a2 = (A) parcel.readValue(A.class.getClassLoader());
                        target.a3 = (A<T>) parcel.readValue(A.class.getClassLoader());
                    }
                }
                """,
                [
                        I: input,
                        _: [
                                Long.class,
                                Override.class,
                                android.os.Parcel.class,
                                Parcelable.class,
                                String.class,
                                Weave.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(a, input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a class with a generic field"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T<T> implements Parcelable {

                    T data;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR =
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static <T> void writeToParcel(#I<T> target, Parcel parcel) {
                        parcel.writeValue(target.data);
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static <T> void readFromParcel(#I<T> target, Parcel parcel) {
                        target.data = (T) parcel.readValue(null);
                    }
                }
                """,
                [
                        I: input,
                        _: [
                                Override.class,
                                android.os.Parcel.class,
                                Parcelable.class,
                                Weave.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }

    def "generate _Helper for a class with a generic field extends String"() {
        given:
        final JavaFileObject input = JavaFile.newFile("com.example", "MyClass",
                """
                @#P
                public class #T<T extends String> implements Parcelable {

                    T data;

                    public #T(android.os.Parcel p) {
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(android.os.Parcel dest, int flags) {
                    }
                }
                """,
                [
                        P: Parcel.class,
                        _: [Parcelable.class]
                ]
        )

        expect:
        final JavaFileObject expected = JavaFile.newGeneratedFile("com.example", "MyClass_Helper",
                """
                @WeaveInto(
                    target = "com.example.#I"
                )
                abstract class #T {

                    @Weave(
                        into = "<FIELD>",
                        statement = "com.example.#T.CREATOR"
                    )
                    public static final Parcelable.Creator<#I> CREATOR =
                      new Parcelable.Creator<#I>() {
                        @Override
                        public #I createFromParcel(Parcel in) {
                            return new #I(in);
                        }
                        @Override
                        public #I[] newArray(int size) {
                            return new #I[size];
                        }
                    };

                    @Weave(
                        into = "0>writeToParcel",
                        args = {"android.os.Parcel", "int"},
                        statement = "com.example.#T.writeToParcel(this, \$1);"
                    )
                    public static <T extends String> void writeToParcel(#I<T> target, Parcel parcel) {
                        parcel.writeValue(target.data);
                    }

                    @Weave(
                        into = "",
                        args = {"android.os.Parcel"},
                        statement = "com.example.#T.readFromParcel(this, \$1);"
                    )
                    public static <T extends String> void readFromParcel(#I<T> target, Parcel parcel) {
                        target.data = (T) parcel.readValue(String.class.getClassLoader());
                    }
                }
                """,
                [
                        I: input,
                        _: [
                                Override.class,
                                android.os.Parcel.class,
                                Parcelable.class,
                                String.class,
                                Weave.class,
                                WeaveInto.class,
                        ]
                ]
        )

        assertFiles(input)
                .with(BladeProcessor.Module.PARCEL)
                .compilesWithoutError()
                .and()
                .generatesSources(expected)
    }
}