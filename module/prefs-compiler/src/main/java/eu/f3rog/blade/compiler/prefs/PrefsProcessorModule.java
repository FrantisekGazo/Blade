package eu.f3rog.blade.compiler.prefs;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import blade.Prefs;
import eu.f3rog.blade.compiler.ProcessorModule;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link PrefsProcessorModule}
 *
 * @author FrantisekGazo
 * @version 2016-08-09
 */
public class PrefsProcessorModule implements ProcessorModule {

    @Override
    public void process(TypeElement bladeElement) throws ProcessorError {
    }

    @Override
    public void process(RoundEnvironment roundEnv) throws ProcessorError {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Prefs.class);
        for (Element e : elements) {
            ClassManager.getInstance()
                    .getHelper((TypeElement) e.getEnclosingElement())
                    .getModule(PrefsHelperModule.class)
                    .add((TypeElement) e);
        }
    }

}
