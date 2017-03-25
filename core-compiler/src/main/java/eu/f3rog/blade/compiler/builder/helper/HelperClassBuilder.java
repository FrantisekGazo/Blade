package eu.f3rog.blade.compiler.builder.helper;

import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.util.ClassComparator;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link HelperClassBuilder}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class HelperClassBuilder
        extends BaseClassBuilder
        implements IHelper {

    private final TypeElement mTypeElement;
    private final Map<Class<? extends IHelperModule>, IHelperModule> mHelperModuleMap = new HashMap<>();

    public HelperClassBuilder(ClassName className, TypeElement e) throws ProcessorError {
        super(GCN.HELPER, className);
        mTypeElement = e;
    }

    @Override
    public void start() throws ProcessorError {
        super.start();

        getBuilder().addModifiers(Modifier.ABSTRACT);
    }

    @Override
    public <T extends IHelperModule> T getModule(Class<T> cls) throws ProcessorError {
        if (mHelperModuleMap.containsKey(cls)) {
            return (T) mHelperModuleMap.get(cls);
        } else {
            T i;
            try {
                i = cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            i.checkClass(mTypeElement);
            mHelperModuleMap.put(cls, i);
            return i;
        }
    }

    @Override
    public <T extends IHelperModule> T tryGetModule(Class<T> cls) throws ProcessorError {
        if (mHelperModuleMap.containsKey(cls)) {
            return (T) mHelperModuleMap.get(cls);
        } else {
            T i;
            try {
                i = cls.newInstance();
                i.checkClass(mTypeElement);
            } catch (Exception e) {
                return null;
            }
            mHelperModuleMap.put(cls, i);
            return i;
        }
    }

    @Override
    public <T extends IHelperModule> T getModuleIfExists(Class<T> cls) throws ProcessorError {
        if (mHelperModuleMap.containsKey(cls)) {
            return (T) mHelperModuleMap.get(cls);
        } else {
            return null;
        }
    }

    @Override
    public void build() throws ProcessorError, IOException {
        boolean hasSomething = false;

        for (final IHelperModule helperModule : getSortedHelperModules()) {
            hasSomething |= helperModule.implement(this);
        }
        // do not build empty helper class
        if (hasSomething) {
            super.build();
        }
    }

    private List<IHelperModule> getSortedHelperModules() {
        final List<IHelperModule> result = new ArrayList<>();

        final List<Class<? extends IHelperModule>> helperClasses = new ArrayList<>(mHelperModuleMap.keySet());
        Collections.sort(helperClasses, new ClassComparator());

        for (Class<? extends IHelperModule> key : helperClasses) {
            result.add(mHelperModuleMap.get(key));
        }

        return result;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }
}
