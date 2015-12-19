package eu.f3rog.blade.compiler.module.arg;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Arg;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.ProcessorModule;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link ArgProcessorModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public class ArgProcessorModule implements ProcessorModule {

    @Override
    public Class[] getSupportedAnnotations() {
        return new Class[]{
                Arg.class
        };
    }

    @Override
    public void process(RoundEnvironment roundEnv) throws ProcessorError {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Arg.class);
        for (Element e : elements) {
            ClassManager.getInstance()
                    .getHelper((TypeElement) e.getEnclosingElement())
                    .getModule(ArgHelperModule.class)
                    .add((VariableElement) e);
        }
    }

}
