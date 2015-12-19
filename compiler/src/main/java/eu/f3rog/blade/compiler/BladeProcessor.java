package eu.f3rog.blade.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Arg;
import blade.Extra;
import blade.State;
import eu.f3rog.blade.compiler.builder.FragmentFactoryBuilder;
import eu.f3rog.blade.compiler.builder.InjectorBuilder;
import eu.f3rog.blade.compiler.builder.NavigatorBuilder;
import eu.f3rog.blade.compiler.util.BaseProcessor;
import eu.f3rog.blade.compiler.util.ProcessorError;

@AutoService(Processor.class)
public class BladeProcessor extends BaseProcessor {

    private InjectorBuilder mInjectorBuilder = null;

    @Override
    protected Set<Class> getSupportedAnnotationClasses() {
        HashSet<Class> set = new HashSet<>();
        set.add(Arg.class);
        set.add(Extra.class);
        set.add(State.class);
        return set;
    }

    @Override
    protected void prepare(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        mInjectorBuilder = new InjectorBuilder();
    }

    @Override
    protected void exec(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        // add ACTIVITY INJECTORS
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Extra.class);
        for (Element e : elements) {
            mInjectorBuilder.addExtra((VariableElement) e);
        }
        // add FRAGMENT INJECTORS
        elements = roundEnv.getElementsAnnotatedWith(Arg.class);
        for (Element e : elements) {
            mInjectorBuilder.addArg((VariableElement) e);
        }
        // add STATE
        elements = roundEnv.getElementsAnnotatedWith(State.class);
        for (Element e : elements) {
            // TODO
        }
    }

    @Override
    protected void finish(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException {
        // write classes:

        // create NAVIGATOR
        NavigatorBuilder navigatorBuilder = new NavigatorBuilder();
        navigatorBuilder.integrate(mInjectorBuilder.getActivityInjectorBuilders());
        navigatorBuilder.build(getFiler());

        // create FRAGMENT FACTORY
        FragmentFactoryBuilder fragmentFactoryBuilder = new FragmentFactoryBuilder();
        fragmentFactoryBuilder.integrate(mInjectorBuilder.getFragmentInjectorBuilders());
        fragmentFactoryBuilder.build(getFiler());

        mInjectorBuilder.build(getFiler());
    }

}
