package eu.f3rog.automat.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import eu.f3rog.automat.Arg;
import eu.f3rog.automat.Extra;
import eu.f3rog.automat.compiler.builder.ActivityInjectorBuilder;
import eu.f3rog.automat.compiler.builder.FragmentFactoryBuilder;
import eu.f3rog.automat.compiler.builder.FragmentInjectorBuilder;
import eu.f3rog.automat.compiler.builder.InjectorBuilder;
import eu.f3rog.automat.compiler.builder.NavigatorBuilder;
import eu.f3rog.automat.compiler.util.ProcessorError;

@AutoService(javax.annotation.processing.Processor.class)
public class Processor extends AbstractProcessor {

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
            for (Map.Entry<ClassName, ActivityInjectorBuilder> entry : injectorBuilder.getActivityInjectorBuilders().entrySet()) {
                navigatorBuilder.integrate(entry.getValue());
            }
            navigatorBuilder.build(mFiler);
            // create FRAGMENT FACTORY
            FragmentFactoryBuilder fragmentFactoryBuilder = new FragmentFactoryBuilder();
            for (Map.Entry<ClassName, FragmentInjectorBuilder> entry : injectorBuilder.getFragmentInjectorBuilders().entrySet()) {
                fragmentFactoryBuilder.integrate(entry.getValue());
            }
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
