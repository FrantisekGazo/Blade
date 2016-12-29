package eu.f3rog.blade.compiler.module;

import android.os.Bundle;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.util.ProcessorUtils;
import eu.f3rog.blade.core.Bundler;

/**
 * Class {@link BundleUtils}
 *
 * @author FrantisekGazo
 */
public final class BundleUtils {

    public static final class BundledField {
        private final String mName;
        private final ClassName mCustomBundler;

        public BundledField(String name, ClassName customBundler) {
            mName = name;
            mCustomBundler = customBundler;
        }

        public String getName() {
            return mName;
        }

        public ClassName getCustomBundler() {
            return mCustomBundler;
        }
    }

    private static final String FORMAT_BUNDLER_VARIABLE_NAME = "%sBundler";
    private static final String FORMAT_NEW_BUNDLE_VARIABLE_NAME = "%sBundle";

    public static <A extends Annotation> void addBundledField(final Collection<BundledField> fields,
                                                              final VariableElement e,
                                                              final Class<A> annotationClass,
                                                              final ProcessorUtils.IGetter<A, Class<?>> classGetter) {
        final A annotation = e.getAnnotation(annotationClass);
        final ClassName bundlerClass = ProcessorUtils.getClass(annotation, classGetter);

        final String fieldName = e.getSimpleName().toString();

        if (ClassName.get(Bundler.class).equals(bundlerClass)) {
            fields.add(new BundledField(fieldName, null));
        } else { // if custom Bundler class is used
            fields.add(new BundledField(fieldName, bundlerClass));
        }
    }

    public static <A extends Annotation> BundledField getBundledField(final VariableElement e,
                                                                      final Class<A> annotationClass,
                                                                      final ProcessorUtils.IGetter<A, Class<?>> classGetter) {
        final A annotation = e.getAnnotation(annotationClass);
        final ClassName bundlerClass = ProcessorUtils.getClass(annotation, classGetter);

        final String fieldName = e.getSimpleName().toString();

        if (ClassName.get(Bundler.class).equals(bundlerClass)) {
            return new BundledField(fieldName, null);
        } else { // if custom Bundler class is used
            return new BundledField(fieldName, bundlerClass);
        }
    }

    public static void getFromBundle(final MethodSpec.Builder methodBuilder,
                                     final String targetVariableName,
                                     final List<BundledField> targetFields,
                                     final String keyFormat,
                                     final String bundleVariableName) {
        for (int i = 0, c = targetFields.size(); i < c; i++) {
            final BundledField field = targetFields.get(i);
            final String fieldName = field.getName();
            final ClassName customBundler = field.getCustomBundler();

            if (customBundler != null) {
                final String bundlerVarName = String.format(FORMAT_BUNDLER_VARIABLE_NAME, fieldName);

                methodBuilder.addStatement("$T $N = new $T()",
                        customBundler, bundlerVarName, customBundler);
                methodBuilder.addStatement("$N.$N = $N.restore($N.getBundle($S))",
                        targetVariableName, fieldName,
                        bundlerVarName, bundleVariableName, String.format(keyFormat, fieldName));
            } else {
                methodBuilder.addStatement("$N.$N = $N.get($S, $N.$N)",
                        targetVariableName, fieldName,
                        bundleVariableName, String.format(keyFormat, fieldName),
                        targetVariableName, fieldName);
            }
        }
    }

    public static void putToBundle(final MethodSpec.Builder methodBuilder,
                                   final String targetVariableName,
                                   final List<BundledField> targetFields,
                                   final String keyFormat,
                                   final String bundleVariableName) {
        for (int i = 0, c = targetFields.size(); i < c; i++) {
            final BundledField field = targetFields.get(i);
            //putToBundle(methodBuilder, targetVariableName, field, keyFormat, bundleVariableName);
            final String fieldName = field.getName();
            final ClassName customBundler = field.getCustomBundler();

            if (customBundler != null) {
                final String bundlerVarName = String.format(FORMAT_BUNDLER_VARIABLE_NAME, fieldName);
                final String newBundleVarName = String.format(FORMAT_NEW_BUNDLE_VARIABLE_NAME, fieldName);

                methodBuilder.addStatement("$T $N = new $T()",
                        Bundle.class, newBundleVarName, Bundle.class);
                methodBuilder.addStatement("$T $N = new $T()",
                        customBundler, bundlerVarName, customBundler);
                methodBuilder.addStatement("$N.save($N.$N, $N)",
                        bundlerVarName, targetVariableName, fieldName, newBundleVarName);
                methodBuilder.addStatement("$N.put($S, $N)",
                        bundleVariableName, String.format(keyFormat, fieldName), newBundleVarName);
            } else {
                methodBuilder.addStatement("$N.put($S, $N.$N)",
                        bundleVariableName, String.format(keyFormat, fieldName),
                        targetVariableName, fieldName);
            }
        }
    }

    public static void putToBundle(final MethodSpec.Builder methodBuilder,
                                   final String targetVariableName,
                                   final BundledField field,
                                   final String keyFormat,
                                   final String bundleVariableName) {
        final String fieldName = field.getName();
        final ClassName customBundler = field.getCustomBundler();
        final String key = String.format(keyFormat, fieldName);

        if (customBundler != null) {
            final String bundlerVarName = String.format(FORMAT_BUNDLER_VARIABLE_NAME, fieldName);
            final String newBundleVarName = String.format(FORMAT_NEW_BUNDLE_VARIABLE_NAME, fieldName);

            methodBuilder.addStatement("$T $N = new $T()",
                    Bundle.class, newBundleVarName, Bundle.class);
            methodBuilder.addStatement("$T $N = new $T()",
                    customBundler, bundlerVarName, customBundler);
            if (targetVariableName != null) {
                methodBuilder.addStatement("$N.save($N.$N, $N)",
                        bundlerVarName, targetVariableName, fieldName, newBundleVarName);
            } else {
                methodBuilder.addStatement("$N.save($N, $N)",
                        bundlerVarName, fieldName, newBundleVarName);
            }
            methodBuilder.addStatement("$N.put($S, $N)",
                    bundleVariableName, key, newBundleVarName);
        } else {
            if (targetVariableName != null) {
                methodBuilder.addStatement("$N.put($S, $N.$N)",
                        bundleVariableName, key, targetVariableName, fieldName);
            } else {
                methodBuilder.addStatement("$N.put($S, $N)",
                        bundleVariableName, key, fieldName);
            }
        }
    }

    private BundleUtils() {
        throw new IllegalAccessError("This class cannot be instantiated!");
    }
}
