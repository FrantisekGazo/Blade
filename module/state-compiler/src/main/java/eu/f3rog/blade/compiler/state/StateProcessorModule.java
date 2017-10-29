package eu.f3rog.blade.compiler.state;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.State;
import eu.f3rog.blade.compiler.BaseProcessorModule;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link StateProcessorModule}
 *
 * @author FrantisekGazo
 */
public final class StateProcessorModule
        extends BaseProcessorModule {

    @Override
    public void process(final TypeElement bladeElement) throws ProcessorError {
        // do nothing
    }

    @Override
    public void process(final RoundEnvironment roundEnv) throws ProcessorError {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(State.class);
        for (final Element e : elements) {
            ClassManager.getInstance()
                    .getHelper((TypeElement) e.getEnclosingElement())
                    .getModule(StateHelperModule.class)
                    .add((VariableElement) e);
        }
    }

}
