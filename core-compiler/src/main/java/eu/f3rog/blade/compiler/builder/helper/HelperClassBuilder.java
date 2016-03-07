package eu.f3rog.blade.compiler.builder.helper;

import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import eu.f3rog.blade.compiler.builder.BaseClassBuilder;
import eu.f3rog.blade.compiler.name.GCN;
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
    private final Map<Class<? extends IHelperModule>, IHelperModule> mImplementations = new HashMap<>();

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
        if (mImplementations.containsKey(cls)) {
            return (T) mImplementations.get(cls);
        } else {
            T i;
            try {
                i = cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            i.checkClass(mTypeElement);
            mImplementations.put(cls, i);
            return i;
        }
    }

    @Override
    public <T extends IHelperModule> T tryGetModule(Class<T> cls) throws ProcessorError {
        if (mImplementations.containsKey(cls)) {
            return (T) mImplementations.get(cls);
        } else {
            T i;
            try {
                i = cls.newInstance();
                i.checkClass(mTypeElement);
            } catch (Exception e) {
                return null;
            }
            mImplementations.put(cls, i);
            return i;
        }
    }

    @Override
    public <T extends IHelperModule> T getModuleIfExists(Class<T> cls) throws ProcessorError {
        if (mImplementations.containsKey(cls)) {
            return (T) mImplementations.get(cls);
        } else {
            return null;
        }
    }

    @Override
    public void build(ProcessingEnvironment processingEnvironment) throws ProcessorError, IOException {
        boolean hasSomething = false;
        for (Map.Entry<Class<? extends IHelperModule>, IHelperModule> entry : mImplementations.entrySet()) {
            hasSomething |= entry.getValue().implement(processingEnvironment, this);
        }
        // do not build empty helper class
        if (hasSomething) {
            super.build(processingEnvironment);
        }
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }
}
