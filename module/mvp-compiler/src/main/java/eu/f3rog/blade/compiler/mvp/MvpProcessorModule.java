package eu.f3rog.blade.compiler.mvp;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import eu.f3rog.blade.compiler.BaseProcessorModule;
import eu.f3rog.blade.compiler.builder.ClassManager;
import eu.f3rog.blade.compiler.util.ProcessorError;

/**
 * Class {@link MvpProcessorModule}
 *
 * @author FrantisekGazo
 */
public final class MvpProcessorModule
        extends BaseProcessorModule {

    @Override
    public void prepare() throws ProcessorError {
        try {
            // check if dagger dependency is present
            Class inject = Class.forName("javax.inject.Inject");
            Class membersInjector = Class.forName("dagger.internal.MembersInjectors");
        } catch (Exception e) {
            throw new ProcessorError(null, MvpErrorMsg.Missing_dagger_dependency);
        }
    }

    @Override
    public void process(TypeElement bladeElement) throws ProcessorError {
        ClassManager.getInstance()
                .getHelper(bladeElement)
                .tryGetModule(PresenterHelperModule.class);
    }

    @Override
    public void process(RoundEnvironment roundEnv) throws ProcessorError {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Inject.class);
        for (final Element e : elements) {
            // process only field injections
            if (e.getKind() != ElementKind.FIELD) {
                continue;
            }

            TypeElement typeElement = (TypeElement) e.getEnclosingElement();

            PresenterHelperModule module = ClassManager.getInstance()
                    .getHelper(typeElement)
                    .tryGetModule(PresenterHelperModule.class);
            if (module != null) {
                module.add((VariableElement) e);
            }
        }
    }

}
