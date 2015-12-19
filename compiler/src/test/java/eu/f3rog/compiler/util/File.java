package eu.f3rog.compiler.util;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;
import javax.tools.JavaFileObject;

import eu.f3rog.blade.compiler.BladeProcessor;

/**
 * Class {@link File}
 *
 * @author FrantisekGazo
 * @version 2015-11-20
 */
public class File implements IImports, IBody {

    protected static final String CLASS = "T";

    protected static final String GENERATED = String.format("@Generated(\"%s\")", BladeProcessor.class.getName());

    private static final String DELIMITER = "\n";

    private static final String IMPORT = "import %s;";
    private static final String PACKAGE = "package %s;";
    private static final String FQN = "%s.%s";

    private static final CharSequence KEY_SIGN = "$";
    private static final String KEY_FORMAT = "\\" + KEY_SIGN + "%s";

    public static IImports file(String pack, String name) {
        return new File(pack, name);
    }

    public static IImports generatedFile(String pack, String name) {
        return new File(pack, name, Generated.class);
    }

    private final String mName;
    private final String mPackage;
    private LinkedHashSet<Object> mImportClasses;
    private String mLastClassName;
    private String mBeforeBody;
    private Map<String, String> mMapping;

    public File(String pack, String name, Object... imports) {
        mName = name;
        mPackage = pack;
        mImportClasses = new LinkedHashSet<>();
        mLastClassName = null;
        if (imports.length > 0) {
            mBeforeBody = GENERATED;
            mImportClasses.addAll(Arrays.asList(imports));
        } else {
            mBeforeBody = "";
        }
        mMapping = new HashMap<>();
        mMapping.put(CLASS, mName);
    }

    @Override
    public IBody imports(Object... classes) {
        mImportClasses.addAll(Arrays.asList(classes));
        return this;
    }

    private String importClasses(LinkedHashSet<Object> classes) {
        List<String> classNames = new ArrayList<>(classes.size());
        for (Object o : classes) {
            String className = null;
            if (o instanceof Class) {
                className = ((Class) o).getName();
            } else if (o instanceof String) {
                String s = (String) o;
                if (s.contains(".")) {
                    className = s;
                } else if (mLastClassName != null) {
                    // save mapping
                    mMapping.put(s, mLastClassName.substring(mLastClassName.lastIndexOf(".") + 1));
                }
            } else if (o instanceof JavaFileObject) {
                className = ((JavaFileObject) o).getName()
                        .replaceAll(java.io.File.separator, ".")
                        .replace(".java", "");
            }

            if (className != null) {
                String packageName = className.substring(0, className.lastIndexOf("."));
                mLastClassName = className;
                // import only from different packages
                if (!packageName.equals(mPackage)) {
                    classNames.add(className);
                }
            }
        }
        Collections.sort(classNames);
        StringBuilder sb = new StringBuilder();
        for (String className : classNames) {
            sb.append(getImportLine(className)).append(DELIMITER);
        }
        return sb.toString();
    }

    @Override
    public JavaFileObject body(String... lines) {
        String imports = importClasses(mImportClasses);

        for (int i = 0; i < lines.length; i++) {
            lines[i] = performMapping(lines[i]);
        }

        return JavaFileObjects.forSourceString(
                getFullyQualifiedName(),
                Joiner.on(DELIMITER).join(
                        getPackageLine(),
                        imports,
                        mBeforeBody,
                        Joiner.on(DELIMITER).join(lines)
                )
        );
    }

    private String performMapping(final String line) {
        if (!line.contains(KEY_SIGN)) return line;

        String res = line;
        List<String> keys = new ArrayList<>(mMapping.keySet());
        Collections.sort(keys);
        for (int i = keys.size() - 1; i >= 0; i--) {
            String key = keys.get(i);
            res = res.replaceAll(String.format(KEY_FORMAT, key), mMapping.get(key));
        }
        return res;
    }

    private String getFullyQualifiedName() {
        return String.format(FQN, mPackage, mName);
    }

    public String getPackageLine() {
        return String.format(PACKAGE, mPackage);
    }

    private String getImportLine(String classFQN) {
        return String.format(IMPORT, classFQN);
    }
}
