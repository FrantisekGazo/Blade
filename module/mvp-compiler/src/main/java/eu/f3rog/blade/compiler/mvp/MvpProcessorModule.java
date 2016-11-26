package eu.f3rog.blade.compiler.mvp;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.ProcessorModule;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link MvpProcessorModule}
 *
 * @author FrantisekGazo
 */
public final class MvpProcessorModule
        implements ProcessorModule {

    @Override
    public void process(TypeElement bladeElement) throws ProcessorError {
        /*ClassManager.getInstance()
                .getHelper(bladeElement)
                .tryGetModule(PresenterScopeHelperModule.class);*/
    }

    @Override
    public void process(RoundEnvironment roundEnv) throws ProcessorError {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Inject.class);
        for (Element e : elements) {
            TypeElement typeElement = (TypeElement) e.getEnclosingElement();

            PresenterHelperModule module = ClassManager.getInstance()
                    .getHelper(typeElement)
                    .tryGetModule(PresenterHelperModule.class);
            if (module != null) {
                module.add((VariableElement) e);
            }
/*
            ClassManager.getInstance()
                    .getHelper(typeElement)
                    .tryGetModule(PresenterScopeHelperModule.class);*/
        }
    }

}
