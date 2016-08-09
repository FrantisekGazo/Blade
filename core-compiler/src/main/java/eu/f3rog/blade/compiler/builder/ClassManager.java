package eu.f3rog.blade.compiler.builder;

import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;

import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.util.ClassNameComparator;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link ClassManager}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class ClassManager
        implements IBuildable {

    private static ClassManager sInstance;

    public static void init() {
        sInstance = new ClassManager();
    }

    public static ClassManager getInstance() {
        return sInstance;
    }

    private final Map<Class, BaseClassBuilder> mSpecialClasses;
    private final Map<ClassName, HelperClassBuilder> mHelpers;
    private final List<BaseClassBuilder> mOtherBuilders;

    private ClassManager() {
        mSpecialClasses = new HashMap<>();
        mHelpers = new HashMap<>();
        mOtherBuilders = new ArrayList<>();
    }

    public HelperClassBuilder getHelper(TypeElement e) throws ProcessorError {
        ClassName className = ClassName.get(e);
        if (!mHelpers.containsKey(className)) {
            mHelpers.put(className, new HelperClassBuilder(className, e));
        }
        return mHelpers.get(className);
    }

    public HelperClassBuilder getHelperIfExists(TypeElement e) throws ProcessorError {
        ClassName className = ClassName.get(e);
        return mHelpers.get(className);
    }

    public <T extends BaseClassBuilder> T getSpecialClass(Class<T> cls) {
        if (mSpecialClasses.containsKey(cls)) {
            return (T) mSpecialClasses.get(cls);
        } else {
            T instance;
            try {
                instance = cls.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            mSpecialClasses.put(cls, instance);
            return instance;
        }
    }

    @Override
    public void build() throws ProcessorError, IOException {
        List<ClassName> classes = new ArrayList<>(mHelpers.keySet());
        Collections.sort(classes, new ClassNameComparator());
        for (int i = 0, size = classes.size(); i < size; i++) {
            HelperClassBuilder builder = mHelpers.get(classes.get(i));
            // build file
            builder.build();
        }

        // build special classes
        for (Map.Entry<Class, BaseClassBuilder> entry : mSpecialClasses.entrySet()) {
            entry.getValue().build();
        }

        // build other builders
        for (int i = 0; i < mOtherBuilders.size(); i++) {
            mOtherBuilders.get(i).build();
        }
    }

    public void add(final BaseClassBuilder builder) {
        mOtherBuilders.add(builder);
    }
}
