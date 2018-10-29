package eu.f3rog.blade.compiler.util;

import android.app.Activity;
import android.app.Fragment;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import eu.f3rog.blade.compiler.name.ClassNames;

/**
 * Class {@link ProcessorUtils}.
 *
 * @author Frantisek Gazo
 * @version 2015-09-21
 */
public class ProcessorUtils {

    public interface IGetter<A, T> {
        T get(A obj);
    }

    private static ProcessingEnvironment sProcessingEnvironment;

    public static void setProcessingEnvironment(ProcessingEnvironment processingEnvironment) {
        sProcessingEnvironment = processingEnvironment;
    }

    public static Elements getElementUtils() {
        return sProcessingEnvironment.getElementUtils();
    }

    public static Types getTypeUtils() {
        return sProcessingEnvironment.getTypeUtils();
    }

    public static Filer getFiler() {
        return sProcessingEnvironment.getFiler();
    }

    public static String fullName(ClassName className) {
        //return String.format("%s.%s", className.packageName(), className.simpleName());
        StringBuilder sb = new StringBuilder();

        sb.append(className.packageName());

        for (int i = 0, c = className.simpleNames().size(); i < c; i++) {
            String name = className.simpleNames().get(i);
            sb.append(".").append(name);
        }
        return sb.toString();
    }

    /**
     * Discovers if {@code element} is annotated with {@code needed} annotation.
     */
    public static boolean isAnnotated(final Element element, final TypeName needed) {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        // if given element has no annotation => end now
        if (annotationMirrors == null || annotationMirrors.size() == 0) return false;
        // go through all annotation of given element
        for (int i = 0, c = annotationMirrors.size(); i < c; i++) {
            AnnotationMirror annotationMirror = annotationMirrors.get(i);
            // check if found annotation is the same class as needed annotation
            if (needed.equals(ClassName.get(annotationMirror.getAnnotationType().asElement().asType())))
                return true;
        }
        return false;
    }

    /**
     * Retrieves {@link ClassName} from {@code annotation} with {@code getter} and if exception is thrown, retrieves it from the exception.
     */
    public static <A> ClassName getClass(final A annotation, final IGetter<A, Class<?>> getter) {
        ClassName className;
        try {
            className = ClassName.get(getter.get(annotation));
        } catch (MirroredTypeException mte) {
            try {
                className = (ClassName) ClassName.get(((DeclaredType) mte.getTypeMirror()).asElement().asType());
            } catch (Exception e) { // if there is 'primitive'.class
                className = null;
            }
        }
        return className;
    }

    /**
     * Retrieves {@link TypeName} from {@code annotation} with {@code getter} and if exception is thrown, retrieves it from the exception.
     */
    public static <A> TypeName getType(final A annotation, final IGetter<A, Class<?>> getter) {
        TypeName typeName;
        try {
            typeName = ClassName.get(getter.get(annotation));
        } catch (MirroredTypeException mte) {
            typeName = ClassName.get(mte.getTypeMirror());
        }
        return typeName;
    }

    /**
     * Retrieves {@link ClassName}s from {@code annotation} with {@code getter} and if exception is thrown, retrieves it from the exception.
     */
    public static <A> List<ClassName> getParamClasses(final A annotation, final IGetter<A, Class<?>[]> getter) {
        List<ClassName> className = new ArrayList<>();
        try {
            Class<?>[] classes = getter.get(annotation);
            for (int i = 0; i < classes.length; i++) {
                Class cls = classes[i];
                className.add(ClassName.get(cls));
            }
        } catch (MirroredTypesException mte) {
            try {
                List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();
                for (int i = 0, c = typeMirrors.size(); i < c; i++) {
                    TypeMirror typeMirror = typeMirrors.get(i);
                    className.add((ClassName) ClassName.get(((DeclaredType) typeMirror).asElement().asType()));
                }
            } catch (Exception e) { // if there is 'primitive'.class
            }
        }
        return className;
    }

    public static String getParamName(final ClassName className) {
        return StringUtils.startLowerCase(className.simpleName()).replaceAll("_", "");
    }

    public static boolean hasSomeModifier(Element e, Modifier... modifiers) {
        if (e == null) {
            throw new IllegalStateException("Element cannot be null!");
        }
        Set<Modifier> m = e.getModifiers();
        for (int i = 0; i < modifiers.length; i++) {
            if (m.contains(modifiers[i])) return true;
        }
        return false;
    }

    public static boolean cannotHaveAnnotation(Element e) {
        return hasSomeModifier(e, Modifier.PRIVATE, Modifier.PROTECTED, Modifier.FINAL);
    }

    public static TypeElement getTypeElement(TypeName typeName) {
        String className;
        if (typeName instanceof ParameterizedTypeName) {
            className = ((ParameterizedTypeName) typeName).rawType.toString();
        } else {
            className = typeName.toString();
        }
        return sProcessingEnvironment.getElementUtils().getTypeElement(className);
    }

    public static boolean isActivitySubClass(TypeElement inspectedType) {
        return isSubClassOf(inspectedType, Activity.class) ||
               isSubClassOf(inspectedType, ClassNames.SupportActivity) ||
               isSubClassOf(inspectedType, ClassNames.AndroidxActivity);
    }

    public static boolean isFragmentSubClass(TypeElement inspectedType) {
        return isSubClassOf(inspectedType, Fragment.class) ||
               isSubClassOf(inspectedType, ClassNames.SupportFragment) ||
               isSubClassOf(inspectedType, ClassNames.AndroidxFragment);
    }

    public static boolean isSubClassOf(TypeElement inspectedType, Class lookupClass) {
        return getSuperType(inspectedType, lookupClass) != null;
    }

    public static boolean isSubClassOf(TypeElement inspectedType, TypeName lookupType) {
        return getSuperType(inspectedType, lookupType) != null;
    }

    /**
     * Finds requested super-type or interface of given type.
     */
    public static TypeName getSuperType(TypeElement inspectedType, Class lookupClass) {
        return getSuperType(inspectedType.asType(), ClassName.get(lookupClass));
    }

    public static TypeName getSuperType(TypeElement inspectedType, TypeName lookupType) {
        return getSuperType(inspectedType.asType(), lookupType);
    }

    private static TypeName getSuperType(TypeMirror inspectedType, TypeName lookupType) {
        TypeName inspectedTypeName = ClassName.get(inspectedType);
        if (areSameType(inspectedTypeName, lookupType)) {
            return inspectedTypeName;
        }

        List<? extends TypeMirror> supertypes = sProcessingEnvironment.getTypeUtils().directSupertypes(inspectedType);
        for (int i = 0, c = supertypes.size(); i < c; i++) {
            TypeMirror superType = supertypes.get(i);
            TypeName tn = getSuperType(superType, lookupType);
            if (tn != null) {
                return tn;
            }
        }

        return null;
    }

    private static boolean areSameType(TypeName typeName1, TypeName typeName2) {
        if (typeName1 instanceof ParameterizedTypeName) {
            ParameterizedTypeName paramTypeName = (ParameterizedTypeName) typeName1;
            return paramTypeName.rawType.equals(typeName2);
        }
        return typeName1.equals(typeName2);
    }

    /**
     * Returns type of given <code>variableElement</code> or bound type if it is generic type.
     */
    public static Type getBoundedType(VariableElement variableElement) {
        if (variableElement instanceof Symbol.VarSymbol) {
            Symbol.VarSymbol arg = (Symbol.VarSymbol) variableElement;
            Type type = arg.type;
            if (type.getUpperBound() != null) {
                type = type.getUpperBound();
            }
            return type;
        } else {
            throw new IllegalStateException();
        }
    }

    public static TypeVariableName[] getTypeParameterNames(TypeName type) {
        List<? extends TypeParameterElement> targetClassParameters = ProcessorUtils.getTypeElement(type).getTypeParameters();
        TypeVariableName[] parameterTypes = new TypeVariableName[targetClassParameters.size()];
        for (int i = 0, c = parameterTypes.length; i < c; i++) {
            TypeVariableName typeName = TypeVariableName.get(targetClassParameters.get(i));
            parameterTypes[i] = typeName;
        }
        return parameterTypes;
    }

    public static void addClassAsParameter(MethodSpec.Builder method, ClassName targetTypeName, String parameterName) {
        TypeVariableName[] typeParameterNames = getTypeParameterNames(targetTypeName);
        if (typeParameterNames.length > 0) {
            for (int i = 0, c = typeParameterNames.length; i < c; i++) {
                method.addTypeVariable(typeParameterNames[i]);
            }
            method.addParameter(ParameterizedTypeName.get(targetTypeName, typeParameterNames), parameterName);
        } else {
            method.addParameter(targetTypeName, parameterName);
        }
    }

    public static TypeName getRawType(TypeMirror typeMirror) {
        return getRawType(ClassName.get(typeMirror));
    }

    public static TypeName getRawType(final TypeName typeName) {
        if (typeName instanceof ArrayTypeName) {
            ArrayTypeName atn = (ArrayTypeName) typeName;
            return getRawType(atn.componentType);
        } else if (typeName instanceof ParameterizedTypeName) {
            ParameterizedTypeName ptn = (ParameterizedTypeName) typeName;
            return ptn.rawType;
        } else if (typeName instanceof TypeVariableName) {
            TypeVariableName tvn = (TypeVariableName) typeName;
            if (!tvn.bounds.isEmpty()) {
                return tvn.bounds.get(0);
            } else {
                return null;
            }
        } else {
            return typeName;
        }
    }
}
