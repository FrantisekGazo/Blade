package eu.f3rog.automat.compiler.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

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

    public static boolean inplements(final TypeElement element, final Class cls) {
        return inplements(element, ClassName.get(cls));
    }

    public static boolean inplements(final TypeElement element, final ClassName cls) {
        TypeElement superClass = element;
        while (superClass != null) {
            for (int i = 0; i < superClass.getInterfaces().size(); i++) {
                if (ClassName.get(superClass.getInterfaces().get(i)).equals(cls)) {
                    return true;
                }
            }
            superClass = (TypeElement) ((Symbol.ClassSymbol) superClass).getSuperclass().asElement();
        }
        return false;
    }

    public static boolean isSubClassOf(final TypeElement element, final Class cls) {
        return isSubClassOf(element, ClassName.get(cls));
    }

    public static boolean isSubClassOf(final TypeElement element, final ClassName cls) {
        TypeElement superClass = element;
        do {
            superClass = (TypeElement) ((Symbol.ClassSymbol) superClass).getSuperclass().asElement();
            if (superClass != null && ClassName.get(superClass).equals(cls)) {
                return true;
            }
        } while (superClass != null);
        return false;
    }

    public static boolean isSubClassOf(final TypeElement element, final ClassName... classes) {
        TypeElement superClass = element;
        do {
            superClass = (TypeElement) ((Symbol.ClassSymbol) superClass).getSuperclass().asElement();
            for (ClassName cls : classes) {
                if (superClass != null && ClassName.get(superClass).equals(cls)) {
                    return true;
                }
            }
        } while (superClass != null);
        return false;
    }

    public static String getParamName(final ClassName className) {
        return StringUtils.startLowerCase(className.simpleName()).replaceAll("_", "");
    }

}
