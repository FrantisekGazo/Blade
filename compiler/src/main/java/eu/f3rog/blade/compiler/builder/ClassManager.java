package eu.f3rog.blade.compiler.builder;

import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
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

    public static void init() throws ProcessorError {
        sInstance = new ClassManager();
    }

    public static ClassManager getInstance() {
        return sInstance;
    }

    private final Map<Class, BaseClassBuilder> mSpecialClasses;
    private final Map<ClassName, HelperClassBuilder> mHelpers;

    private ClassManager() throws ProcessorError {
        mSpecialClasses = new HashMap<>();
        mHelpers = new HashMap<>();
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
    public void build(ProcessingEnvironment processingEnvironment) throws ProcessorError, IOException {
        List<ClassName> classes = new ArrayList<>(mHelpers.keySet());
        Collections.sort(classes, new ClassNameComparator());
        for (int i = 0, size = classes.size(); i < size; i++) {
            HelperClassBuilder builder = mHelpers.get(classes.get(i));
            // build file
            builder.build(processingEnvironment);
        }

        // build special classes
        for (Map.Entry<Class, BaseClassBuilder> entry : mSpecialClasses.entrySet()) {
            entry.getValue().build(processingEnvironment);
        }
    }

}
