package eu.f3rog.blade.compiler.util;

import android.app.Activity;
import android.app.Fragment;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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

    public static String fullName(ClassName className) {
        //return String.format("%s.%s", className.packageName(), className.simpleName());
        StringBuilder sb = new StringBuilder();

        sb.append(className.packageName());

        for (int i = 0; i < className.simpleNames().size(); i++) {
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
        for (AnnotationMirror annotationMirror : annotationMirrors) {
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
            for (Class<?> cls : classes) {
                className.add(ClassName.get(cls));
            }
        } catch (MirroredTypesException mte) {
            try {
                for (TypeMirror typeMirror : mte.getTypeMirrors()) {
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
        return isSubClassOf(inspectedType, Activity.class) || isSubClassOf(inspectedType, ClassNames.AppCompatActivity);
    }

    public static boolean isFragmentSubClass(TypeElement inspectedType) {
        return isSubClassOf(inspectedType, Fragment.class) || isSubClassOf(inspectedType, ClassNames.SupportFragment);
    }

    public static boolean isSubClassOf(TypeElement inspectedType, Class lookupClass) {
        return getSuperType(inspectedType, lookupClass) != null;
    }

    public static boolean isSubClassOf(TypeElement inspectedType, TypeName lookupType) {
        return getSuperType(inspectedType, lookupType) != null;
    }

    /**
     * Finds requested supertype (can be also interface) of given type.
     */
    public static TypeName getSuperType(TypeElement inspectedType, Class lookupClass) {
        return getSuperType(inspectedType.asType(), ClassName.get(lookupClass));
    }

    public static TypeName getSuperType(TypeElement inspectedType, TypeName lookupType) {
        return getSuperType(inspectedType.asType(), lookupType);
    }

    private static TypeName getSuperType(TypeMirror inspectedType, TypeName lookupType) {
        TypeName tn = isSameType(inspectedType, lookupType);
        if (tn != null) {
            return tn;
        }

        List<? extends TypeMirror> superTypes = ProcessorUtils.getTypeUtils().directSupertypes(inspectedType);

        for (TypeMirror typeMirror : superTypes) {
            tn = getSuperType(typeMirror, lookupType);
            if (tn != null) {
                return tn;
            }
        }

        return null;
    }

    private static TypeName isSameType(TypeMirror typeMirror, TypeName lookupType) {
        TypeName tn = ClassName.get(typeMirror);
        if (tn instanceof ParameterizedTypeName) {
            ParameterizedTypeName paramTypeName = (ParameterizedTypeName) tn;
            if (paramTypeName.rawType.equals(lookupType)) {
                return paramTypeName;
            }
        } else if (tn.equals(lookupType)) {
            return tn;
        }
        return null;
    }

}
