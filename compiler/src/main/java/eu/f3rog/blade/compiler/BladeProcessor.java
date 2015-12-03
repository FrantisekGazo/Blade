package eu.f3rog.blade.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import blade.Arg;
import blade.Extra;
import eu.f3rog.blade.compiler.builder.FragmentFactoryBuilder;
import eu.f3rog.blade.compiler.builder.InjectorBuilder;
import eu.f3rog.blade.compiler.builder.NavigatorBuilder;
import eu.f3rog.blade.compiler.util.ProcessorError;

@AutoService(Processor.class)
public class BladeProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Filer mFiler;

    private boolean mProcessed;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(Extra.class.getCanonicalName());
        annotations.add(Arg.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!mProcessed) {
            mProcessed = true;
        } else {
            return false;
        }

        try {
            // create main INJECTOR
            InjectorBuilder injectorBuilder = new InjectorBuilder();

            // create ACTIVITY INJECTORS
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Extra.class);
            for (Element e : elements) {
                injectorBuilder.addExtra((VariableElement) e);
            }
            // create FRAGMENT INJECTORS
            elements = roundEnv.getElementsAnnotatedWith(Arg.class);
            for (Element e : elements) {
                injectorBuilder.addArg((VariableElement) e);
            }

            injectorBuilder.build(mFiler);

            // create NAVIGATOR
            NavigatorBuilder navigatorBuilder = new NavigatorBuilder();
            navigatorBuilder.integrate(injectorBuilder.getActivityInjectorBuilders());
            navigatorBuilder.build(mFiler);
            // create FRAGMENT FACTORY
            FragmentFactoryBuilder fragmentFactoryBuilder = new FragmentFactoryBuilder();
            fragmentFactoryBuilder.integrate(injectorBuilder.getFragmentInjectorBuilders());
            fragmentFactoryBuilder.build(mFiler);
        } catch (ProcessorError pe) {
            error(pe);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private void error(ProcessorError error) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, error.getMessage(), error.getElement());
    }

    private void error(Element e, String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

}
