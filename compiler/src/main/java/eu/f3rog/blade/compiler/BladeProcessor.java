package eu.f3rog.blade.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.module.arg.ArgProcessorModule;
import eu.f3rog.blade.compiler.module.extra.ExtraProcessorModule;
import eu.f3rog.blade.compiler.util.BaseProcessor;
import eu.f3rog.blade.compiler.util.ProcessorError;

@AutoService(Processor.class)
public class BladeProcessor extends BaseProcessor {

    private List<? extends ProcessorModule> mModules;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mModules = Arrays.asList(
                new ArgProcessorModule(),
                new ExtraProcessorModule()
        );
    }

    @Override
    protected Set<Class> getSupportedAnnotationClasses() {
        HashSet<Class> set = new HashSet<>();
        for (int i = 0; i < mModules.size(); i++) {
            Class[] a = mModules.get(i).getSupportedAnnotations();
            for (int j = 0; j < a.length; j++) {
                set.add(a[j]);
            }
        }
        return set;
    }

    @Override
    protected void prepare(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        ClassManager.init();
    }

    @Override
    protected void exec(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        for (int i = 0; i < mModules.size(); i++) {
            mModules.get(i).process(roundEnv);
        }
    }

    @Override
    protected void finish(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        // create class files
        ClassManager.getInstance().build(getFiler());
    }

}