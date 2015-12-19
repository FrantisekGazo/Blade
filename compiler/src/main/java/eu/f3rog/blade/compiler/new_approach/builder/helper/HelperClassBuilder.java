package eu.f3rog.blade.compiler.new_approach.builder.helper;

import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import eu.f3rog.blade.compiler.name.GCN;
import eu.f3rog.blade.compiler.new_approach.builder.BaseClassBuilder;
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

        getBuilder().addModifiers(Modifier.PUBLIC, Modifier.FINAL);
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
    public <T extends IHelperModule> T getModuleIfExists(Class<T> cls) throws ProcessorError {
        if (mImplementations.containsKey(cls)) {
            return (T) mImplementations.get(cls);
        } else {
            return null;
        }
    }

    @Override
    public void build(Filer filer) throws ProcessorError, IOException {
        for (Map.Entry<Class<? extends IHelperModule>, IHelperModule> entry : mImplementations.entrySet()) {
            entry.getValue().implement(this);
        }
        super.build(filer);
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }
}
