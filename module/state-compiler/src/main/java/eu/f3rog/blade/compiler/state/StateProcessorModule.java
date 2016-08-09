package eu.f3rog.blade.compiler.state;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.State;
import eu.f3rog.blade.compiler.ProcessorModule;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link StateProcessorModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-19
 */
public final class StateProcessorModule
        implements ProcessorModule {

    @Override
    public void process(TypeElement bladeElement) throws ProcessorError {
        // do nothing
    }

    @Override
    public void process(RoundEnvironment roundEnv) throws ProcessorError {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(State.class);
        for (Element e : elements) {
            ClassManager.getInstance()
                    .getHelper((TypeElement) e.getEnclosingElement())
                    .getModule(StateHelperModule.class)
                    .add((VariableElement) e);
        }
    }

}
