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

    public static final String[] MODULE_CLASS_NAMES = {
            "eu.f3rog.blade.compiler.arg.ArgProcessorModule",
            "eu.f3rog.blade.compiler.extra.ExtraProcessorModule",
            "eu.f3rog.blade.compiler.state.StateProcessorModule",
            "eu.f3rog.blade.compiler.mvp.MvpProcessorModule"
    };

    private List<ProcessorModule> mModules;

    @Override
    protected void prepare(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        ProcessorUtils.setProcessingEnvironment(getProcessingEnvironment());
        ClassManager.init();
        initModules();
    }

    private void initModules() {
        mModules = new ArrayList<>();
        for (String moduleClassName : MODULE_CLASS_NAMES) {
            try {
                Class<ProcessorModule> moduleClass = (Class<ProcessorModule>) Class.forName(moduleClassName);
                ProcessorModule module = moduleClass.newInstance();
                mModules.add(module);
                //System.out.println("> APT using " + moduleClass.getSimpleName());
            } catch (Exception ignore) {
                // module is not accessible
            }
        }
    }

    @Override
    protected void exec(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        for (Element e : roundEnv.getElementsAnnotatedWith(Blade.class)) {
            if (e.getKind() == ElementKind.CLASS) {
                ClassManager.getInstance().getHelper((TypeElement) e);
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