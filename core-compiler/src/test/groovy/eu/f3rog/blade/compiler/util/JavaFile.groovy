package eu.f3rog.blade.compiler.util

import com.google.testing.compile.JavaFileObjects

import javax.annotation.Generated
import javax.tools.JavaFileObject


public final class JavaFile {

    private static final String DEFAULT_IMPORT_KEY = '_'
    private static final String FQN = '%s.%s'
    private static final String PACKAGE = 'package %s;'
    private static final String IMPORT = 'import %s;'
    private static final String KEY_SIGN = '#'
    private static final String CLASS_KEY = 'T'
    private static final String GENERATED = '@Generated("by Bl@de")';

    public static JavaFileObject newGeneratedFile(final String packageName,
                                                  final String name,
                                                  final String body,
                                                  final Map<String, Object> imports) {
        return createNewFile(true, packageName, name, body, imports)
    }

    public static JavaFileObject newFile(final String packageName,
                                         final String name,
                                         final String body) {
        return createNewFile(false, packageName, name, body, new HashMap<String, Object>())
    }

    public static JavaFileObject newFile(final String packageName,
                                         final String name,
                                         final String body,
                                         final Map<String, Object> imports) {
        return createNewFile(false, packageName, name, body, imports)
    }

    private static JavaFileObject createNewFile(final boolean generated,
                                                final String packageName,
                                                final String name,
                                                final String body,
                                                final Map<String, Object> imports) {
        final String fullyQualifiedName = String.format(FQN, packageName, name)
        final StringBuilder source = new StringBuilder()

        // add package
        source.append(String.format(PACKAGE, packageName)).append('\n')
        // add imports
        if (generated) {
            if (imports.containsKey(DEFAULT_IMPORT_KEY)) {
                final List<Object> defaults = (List<Object>) imports.get(DEFAULT_IMPORT_KEY)
                defaults.add(Generated.class)
            } else {
                final List<Object> defaults = [Generated.class]
                imports.put(DEFAULT_IMPORT_KEY, defaults)
            }
        }
        final Map<String, String> mapping = processImports(source, packageName, imports)
        // annotate body
        if (generated) {
            source.append(GENERATED).append('\n')
        }
        // add body
        mapping.put(CLASS_KEY, name)
        final String processedBody = processBody(body, mapping)
        source.append(processedBody)

        return JavaFileObjects.forSourceString(fullyQualifiedName, source.toString())
    }

    private static Map<String, String> processImports(final StringBuilder source,
                                                      final String packageName,
                                                      final Map<String, Object> imports) {
        final Map<String, String> mapping = new LinkedHashMap<>()

        final List<String> importClasses = new ArrayList<>()

        Set<String> keys = imports.keySet()
        for (final String key : keys) {
            final Object value = imports.get(key)
            if (key.equals(DEFAULT_IMPORT_KEY)) {
                if (value instanceof List) {
                    final List<Object> list = (List<Object>) value
                    for (final Object o : list) {
                        addImport(importClasses, packageName, o)
                    }
                }
            } else {
                final String mappingValue = addImport(importClasses, packageName, value)
                mapping.put(key, mappingValue)
            }
        }

        Collections.sort(importClasses)
        for (final String className : importClasses) {
            source.append(String.format(IMPORT, className)).append('\n')
        }

        return mapping
    }

    private static String addImport(final List<String> importClasses,
                                    final String packageName,
                                    final Object value) {
        final String className = null

        if (value instanceof Class) {
            className = ((Class) value).getName()
        } else if (value instanceof String) {
            className = (String) value
        } else if (value instanceof JavaFileObject) {
            className = ((JavaFileObject) value).getName()
                    .replace(java.io.File.separator, ".")
                    .replace(".java", "");
        }

        if (className != null) {
            String importPackageName = className.substring(0, className.lastIndexOf("."))
            // import only from different packages
            if (!importPackageName.equals(packageName)) {
                importClasses.add(className)
            }

            return className.substring(className.lastIndexOf(".") + 1)
        } else {
            return null
        }
    }

    private static String processBody(final String body, final Map<String, String> mapping) {
        if (!body.contains(KEY_SIGN)) return body

        String res = body

        final List<String> keys = new ArrayList<>(mapping.keySet())
        Collections.sort(keys)
        Collections.reverse(keys)

        for (final String key : keys) {
            res = res.replace(KEY_SIGN + key, mapping.get(key))
        }

        return res
    }
}
