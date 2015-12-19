package eu.f3rog.blade.compiler.new_approach.module.extra;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Extra;
import eu.f3rog.blade.compiler.new_approach.builder.ClassManager;
import eu.f3rog.blade.compiler.new_approach.processor.ProcessorModule;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link ExtraProcessorModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class ExtraProcessorModule implements ProcessorModule {

    @Override
    public Class[] getSupportedAnnotations() {
        return new Class[]{
                Extra.class
        };
    }

    @Override
    public void process(RoundEnvironment roundEnv) throws ProcessorError {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Extra.class);
        for (Element e : elements) {
            ClassManager.getInstance()
                    .getHelper((TypeElement) e.getEnclosingElement())
                    .getModule(ExtraHelperModule.class)
                    .add((VariableElement) e);
        }
    }

}
