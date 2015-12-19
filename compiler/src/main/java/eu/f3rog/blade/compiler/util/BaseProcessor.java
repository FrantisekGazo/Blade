package eu.f3rog.blade.compiler.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public abstract class BaseProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Filer mFiler;
    private boolean mProcessingStarted;

    protected abstract Set<Class> getSupportedAnnotationClasses();

    /**
     * Called only once when annotation processing starts.
     */
    protected abstract void prepare(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException;

    /**
     * Can be called multiple times during annotation processing.
     */
    protected abstract void exec(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException;

    /**
     * Called only once when annotation processing ends.
     */
    protected abstract void finish(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessorError, IOException;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mProcessingStarted = false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supported = new HashSet<>();
        Set<Class> annotations = getSupportedAnnotationClasses();
        for (Class annotation : annotations) {
            supported.add(annotation.getCanonicalName());
        }
        return supported;
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (!mProcessingStarted) {
                prepare(annotations, roundEnv);
                mProcessingStarted = true;
            }

            exec(annotations, roundEnv);

            if (roundEnv.processingOver()) {
                finish(annotations, roundEnv);
            }
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

    public Filer getFiler() {
        return mFiler;
    }

    public Messager getMessager() {
        return mMessager;
    }

}