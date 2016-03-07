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
        MVP("eu.f3rog.blade.compiler.mvp.MvpProcessorModule");

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
                //System.out.println("> APT using " + moduleClass.getSimpleName());
            } catch (Exception ignore) {
                // module is not accessible
            }
        }
    }

    @Override
    protected void prepare(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        ProcessorUtils.setProcessingEnvironment(getProcessingEnvironment());
        ClassManager.init();
    }

    @Override
    protected void exec(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        for (Element e : roundEnv.getElementsAnnotatedWith(Blade.class)) {
            if (e.getKind() == ElementKind.CLASS) {
                for (int i = 0; i < mModules.size(); i++) {
                    mModules.get(i).process(getProcessingEnvironment(), (TypeElement) e);
                }
            }
        }

        for (int i = 0; i < mModules.size(); i++) {
            mModules.get(i).process(getProcessingEnvironment(), roundEnv);
        }
    }

    @Override
    protected void finish(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        // create class files
        ClassManager.getInstance().build(getProcessingEnvironment());
    }

}