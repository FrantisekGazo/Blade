package eu.f3rog.blade.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import blade.Blade;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.util.BaseProcessor;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"*"})
public class BladeProcessor extends BaseProcessor {

    public enum Module {

        ARG("eu.f3rog.blade.compiler.arg.ArgProcessorModule"),
        EXTRA("eu.f3rog.blade.compiler.extra.ExtraProcessorModule"),
        STATE("eu.f3rog.blade.compiler.state.StateProcessorModule"),
        MVP("eu.f3rog.blade.compiler.mvp.MvpProcessorModule"),
        PARCEL("eu.f3rog.blade.compiler.parcel.ParcelProcessorModule");

        private String mPath;

        Module(String path) {
            mPath = path;
        }

        @Override
        public String toString() {
            return mPath;
        }

    }

    private List<ProcessorModule> mModules;

    public BladeProcessor() {
        this(Module.values()); // try all modules
    }

    public BladeProcessor(Module... tryModuleClassNames) {
        mModules = new ArrayList<>();
        for (Module moduleClassName : tryModuleClassNames) {
            try {
                Class<ProcessorModule> moduleClass = (Class<ProcessorModule>) Class.forName(moduleClassName.toString());
                ProcessorModule module = moduleClass.newInstance();
                mModules.add(module);
                log("> %s APT using %s", BladeProcessor.class.getSimpleName(), moduleClass.getSimpleName());
            } catch (Exception ignore) {
                // module is not accessible
            }
        }
    }

    @Override
    protected void prepare(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        log("> %s prepare", BladeProcessor.class.getSimpleName());

        ProcessorUtils.setProcessingEnvironment(getProcessingEnvironment());
        ClassManager.init();

        for (int i = 0; i < mModules.size(); i++) {
            mModules.get(i).prepare();
        }

        log("> %s prepare done", BladeProcessor.class.getSimpleName());
    }

    @Override
    protected void exec(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        log("> %s exec [processingOver:%b]", BladeProcessor.class.getSimpleName(), roundEnv.processingOver());

        for (Element e : roundEnv.getElementsAnnotatedWith(Blade.class)) {
            if (e.getKind() == ElementKind.CLASS) {
                for (int i = 0; i < mModules.size(); i++) {
                    mModules.get(i).process((TypeElement) e);
                }
            }
        }

        for (int i = 0; i < mModules.size(); i++) {
            mModules.get(i).process(roundEnv);
        }

        log("> %s exec done", BladeProcessor.class.getSimpleName());
    }

    @Override
    protected void finish(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        // create class files
        log("> %s finish", BladeProcessor.class.getSimpleName());

        ClassManager.getInstance().build();

        log("> %s finish done", BladeProcessor.class.getSimpleName());
    }

}