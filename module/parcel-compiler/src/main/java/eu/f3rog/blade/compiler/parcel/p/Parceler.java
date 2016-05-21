package eu.f3rog.blade.compiler.parcel.p;

import android.os.Bundle;
import android.os.Parcelable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.isSubClassOf;

/**
 * Class {@link Parceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-23
 */
final public class Parceler {

    private static final Map<String, ClassParceler> sDirectMapper = new HashMap<>();
    private static final Map<String, ClassParceler> sArrayInheritanceMapper = new HashMap<>();
    private static final List<ClassParceler> sInheritanceLister = new ArrayList<>();
    private static final ClassParceler sObjectParceler = new ObjectClassParceler();

    static {
        // DIRECT MAPPINGS:
        // primitive types
        addDirectMapping(new PrimitiveTypeParceler(byte.class));
        addDirectMapping(new BooleanClassParceler());
        addDirectMapping(new PrimitiveTypeParceler(int.class));
        addDirectMapping(new PrimitiveTypeParceler(long.class));
        addDirectMapping(new PrimitiveTypeParceler(double.class));
        addDirectMapping(new PrimitiveTypeParceler(float.class));
        addDirectMapping(new PrimitiveTypeParceler(String.class));
        // Bundle is not primitive but has same Parceler syntax
        addDirectMapping(new PrimitiveTypeParceler(Bundle.class));
        // primitive arrays
        addDirectMapping(new PrimitiveArrayParceler(byte[].class, "Byte"));
        addDirectMapping(new PrimitiveArrayParceler(boolean[].class, "Boolean"));
        addDirectMapping(new PrimitiveArrayParceler(int[].class, "Int"));
        addDirectMapping(new PrimitiveArrayParceler(long[].class, "Long"));
        addDirectMapping(new PrimitiveArrayParceler(double[].class, "Double"));
        addDirectMapping(new PrimitiveArrayParceler(float[].class, "Float"));
        addDirectMapping(new PrimitiveArrayParceler(char[].class, "Char"));
        addDirectMapping(new PrimitiveArrayParceler(String[].class, "String"));
        addDirectMapping(new SparseBooleanArrayParceler());
        addDirectMapping(new ObjectArrayParceler());
        // Parcelable array
        addDirectMapping(new PrimitiveArrayParceler(Parcelable[].class, "Parcelable"));


        // INHERITANCE MAPPINGS:
        addInheritanceMapping(new ParcelableClassParceler());
        addInheritanceMapping(new SerializableClassParceler());
        addInheritanceMapping(new SparseArrayParceler());


        // ARRAY INHERITANCE
        addArrayInheritanceMapping(new TypedArrayParceler());
        addArrayInheritanceMapping(new ObjectArrayParceler());
    }

    private static void addDirectMapping(ClassParceler cp) {
        if (sDirectMapper.containsKey(cp.getClass().getName())) {
            throw new IllegalStateException();
        }
        sDirectMapper.put(cp.type().getCanonicalName(), cp);
    }

    private static void addInheritanceMapping(ClassParceler cp) {
        sInheritanceLister.add(cp);
    }

    private static void addArrayInheritanceMapping(ClassParceler cp) {
        if (sArrayInheritanceMapper.containsKey(cp.getClass().getName())) {
            throw new IllegalStateException();
        }
        sArrayInheritanceMapper.put(cp.type().getCanonicalName(), cp);
    }

    private static ClassParceler findParceler(VariableElement ve) throws ProcessorError {
        // find direct
        ClassParceler parceler = sDirectMapper.get(ve.asType().toString());
        if (parceler != null) {
            return parceler;
        }

        // find inheritance
        Elements elementsUtils = ProcessorUtils.getElementUtils();
        TypeName tn = ClassName.get(ve.asType());

        if (ve.asType().getKind() == TypeKind.ARRAY) { // is array
            TypeElement te = elementsUtils.getTypeElement(removeArrayParenthesis(tn.toString()));
            if (isSubClassOf(te, Parcelable.class)) {
                return sArrayInheritanceMapper.get("android.os.Parcelable[]");
            } else {
                return sArrayInheritanceMapper.get("java.lang.Object[]");
            }
        }

        // is parametrized or object
        if (tn instanceof ParameterizedTypeName) {
            ParameterizedTypeName ptn = (ParameterizedTypeName) tn;
            tn = ptn.rawType;
        }
        TypeElement lookupType = elementsUtils.getTypeElement(tn.toString());

        if (lookupType != null) {
            for (int i = 0, c = sInheritanceLister.size(); i < c; i++) {
                parceler = sInheritanceLister.get(i);
                if (isSubClassOf(lookupType, parceler.type())) {
                    return parceler;
                }
            }
        }

        return sObjectParceler;
    }

    private static boolean isCollection(TypeElement lookupType) {
        return isSubClassOf(lookupType, ClassName.get("java.util", "Collection"))
                || isSubClassOf(lookupType, ClassName.get("java.util", "Map"));
    }

    /**
     * Returns write call format.
     */
    public static CallFormat writeCall(VariableElement variable) throws ProcessorError {
        ClassParceler parceler = findParceler(variable);
        if (parceler == null) {
            throw null;
        }
        return parceler.writeCall();
    }

    /**
     * Adds read from <code>parcel</code> statement to given <code>method</code>
     */
    public static CallFormat readCall(VariableElement e) throws ProcessorError {
        ClassParceler parceler = findParceler(e);
        if (parceler == null) {
            return null;
        }
        return parceler.readCall();
    }

    public static String removeArrayParenthesis(String typeName) {
        return typeName.replaceAll("\\[\\]", "");
    }

}
