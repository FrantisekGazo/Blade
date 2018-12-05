package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.ArrayTypeName;
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

import eu.f3rog.blade.compiler.name.ClassNames;
import eu.f3rog.blade.compiler.name.NameUtils;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.isSubClassOf;

/**
 * Class {@link Parceler}
 *
 * @author FrantisekGazo
 */
final public class Parceler {

    private static final Map<String, BaseParceler> sDirectMapper = new HashMap<>();
    private static final Map<String, BaseParceler> sArrayInheritanceMapper = new HashMap<>();
    private static final List<BaseParceler> sInheritanceLister = new ArrayList<>();
    private static final BaseParceler sObjectParceler = new ObjectClassParceler();

    private static void init() {
        // DIRECT MAPPINGS:

        // primitive types
        addDirectMapping(new PrimitiveTypeParceler(TypeName.BYTE));
        addDirectMapping(new BooleanParceler());
        addDirectMapping(new PrimitiveTypeParceler(TypeName.INT));
        addDirectMapping(new PrimitiveTypeParceler(TypeName.LONG));
        addDirectMapping(new PrimitiveTypeParceler(TypeName.DOUBLE));
        addDirectMapping(new PrimitiveTypeParceler(TypeName.FLOAT));

        addDirectMapping(new StringParceler());
        addDirectMapping(new BundleParceler());

        // primitive arrays
        addDirectMapping(new PrimitiveArrayParceler(TypeName.BYTE, "Byte"));
        addDirectMapping(new PrimitiveArrayParceler(TypeName.BOOLEAN, "Boolean"));
        addDirectMapping(new PrimitiveArrayParceler(TypeName.INT, "Int"));
        addDirectMapping(new PrimitiveArrayParceler(TypeName.LONG, "Long"));
        addDirectMapping(new PrimitiveArrayParceler(TypeName.DOUBLE, "Double"));
        addDirectMapping(new PrimitiveArrayParceler(TypeName.FLOAT, "Float"));
        addDirectMapping(new PrimitiveArrayParceler(TypeName.CHAR, "Char"));
        addDirectMapping(new PrimitiveArrayParceler(ClassName.get(String.class), "String"));
        addDirectMapping(new SparseBooleanArrayParceler());
        addDirectMapping(new ObjectArrayParceler());
        // Parcelable array
        addDirectMapping(new PrimitiveArrayParceler(ClassNames.Parcelable.get(), "Parcelable"));


        // INHERITANCE MAPPINGS:
        addInheritanceMapping(new ParcelableClassParceler());
        addInheritanceMapping(new SerializableClassParceler());
        addInheritanceMapping(new SparseArrayParceler());


        // ARRAY INHERITANCE
        addArrayInheritanceMapping(new TypedArrayParceler());
        addArrayInheritanceMapping(new ObjectArrayParceler());
    }

    private static void addDirectMapping(BaseParceler cp) {
        if (sDirectMapper.containsKey(cp.getClass().getName())) {
            throw new IllegalStateException();
        }
        sDirectMapper.put(NameUtils.toFQDN(cp.type()), cp);
    }

    private static void addInheritanceMapping(BaseParceler cp) {
        sInheritanceLister.add(cp);
    }

    private static void addArrayInheritanceMapping(BaseParceler cp) {
        if (sArrayInheritanceMapper.containsKey(cp.getClass().getName())) {
            throw new IllegalStateException();
        }
        sArrayInheritanceMapper.put(NameUtils.toFQDN(cp.type()), cp);
    }

    private static BaseParceler findParceler(VariableElement ve) throws ProcessorError {
        if (sDirectMapper.isEmpty()) {
            init();
        }

        // find direct
        BaseParceler parceler = sDirectMapper.get(ve.asType().toString());
        if (parceler != null) {
            return parceler;
        }

        // find inheritance
        Elements elementsUtils = ProcessorUtils.getElementUtils();
        TypeName tn = ClassName.get(ve.asType());

        if (ve.asType().getKind() == TypeKind.ARRAY) { // is array
            TypeElement te = elementsUtils.getTypeElement(removeArrayParenthesis(tn.toString()));
            if (isSubClassOf(te, ClassNames.Parcelable.get())) {
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
        BaseParceler parceler = findParceler(variable);
        if (parceler == null) {
            throw null;
        }
        return parceler.writeCall();
    }

    /**
     * Adds read from <code>parcel</code> statement to given <code>method</code>
     */
    public static CallFormat readCall(VariableElement e) throws ProcessorError {
        BaseParceler parceler = findParceler(e);
        if (parceler == null) {
            return null;
        }
        return parceler.readCall();
    }

    public static String removeArrayParenthesis(String typeName) {
        return typeName.replaceAll("\\[\\]", "");
    }

}
