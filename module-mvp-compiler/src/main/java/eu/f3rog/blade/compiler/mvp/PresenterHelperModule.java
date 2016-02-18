package eu.f3rog.blade.compiler.mvp;

import android.view.View;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.Presenter;
import blade.mvp.IPresenter;
import blade.mvp.IView;
import blade.mvp.PresenterManager;
import eu.f3rog.blade.compiler.ErrorMsg;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.builder.weaving.WeaveBuilder;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.cannotHaveAnnotation;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.fullName;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.getTypeElement;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.isSubClassOf;

/**
 * Class {@link PresenterHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class PresenterHelperModule extends BaseHelperModule {

    private static final String METHOD_NAME_CREATE_PRESENTERS = "setPresenters";
    private static final String METHOD_NAME_UNBIND_PRESENTERS = "unbindPresenters";

    private static final int DATA_ARG = 1;

    private TypeName mPresenterDataType = null;
    private List<String> mPresenters = new ArrayList<>();
    private List<TypeName> mPresenterTypes = new ArrayList<>();

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        if (!isSubClassOf(e, View.class) || !isSubClassOf(e, IView.class)) {
            throw new ProcessorError(e, MvpErrorMsg.Invalid_class_with_Presenter);
        }
    }

    @Override
    public void add(VariableElement e) throws ProcessorError {
        if (cannotHaveAnnotation(e)) {
            throw new ProcessorError(e, ErrorMsg.Invalid_field_with_annotation, Presenter.class.getSimpleName());
        }

        TypeName presenterTypeName = ClassName.get(e.asType());
        TypeElement presenterTypeElement = getTypeElement(presenterTypeName);

        if (presenterTypeElement.getKind() == ElementKind.INTERFACE
                || presenterTypeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ProcessorError(e, MvpErrorMsg.Invalid_Presenter_class);
        }

        if (!presenterTypeElement.getTypeParameters().isEmpty()) {
            throw new ProcessorError(e, MvpErrorMsg.Presenter_class_cannot_be_parametrized);
        }

        if (!hasDefaultConstructor(presenterTypeElement)) {
            throw new ProcessorError(presenterTypeElement, MvpErrorMsg.Presenter_class_missing_default_constructor);
        }

        TypeName dataType = getPresenterDataType(presenterTypeElement);

        if (dataType == null) {
            throw new ProcessorError(e, MvpErrorMsg.Invalid_Presenter_class);
        }

        if (mPresenterDataType == null) {
            mPresenterDataType = dataType;
        } else {
            if (!mPresenterDataType.equals(dataType)) {
                throw new ProcessorError(e, MvpErrorMsg.Inconsistent_Presenter_parameter_classes);
            }
        }

        mPresenters.add(e.getSimpleName().toString());
        mPresenterTypes.add(presenterTypeName);
    }

    @Override
    public void implement(ProcessingEnvironment processingEnvironment, HelperClassBuilder builder) throws ProcessorError {
        addSetPresenterMethod(builder);
        addUnbindPresenterMethod(builder);
    }

    private void addSetPresenterMethod(HelperClassBuilder builder) {
        String tagObject = "tagObject";
        String target = "target";

        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_CREATE_PRESENTERS)
                .addAnnotation(
                        WeaveBuilder.into("setTag", Object.class)
                                .addStatement("String tag = %s.%s(this, $1); super.setTag(tag); return;",
                                        fullName(builder.getClassName()), METHOD_NAME_CREATE_PRESENTERS)
                                .build()
                )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target)
                .addParameter(Object.class, tagObject)
                .returns(String.class);

        String param = "param";

        method.beginControlFlow("if ($N == null)", tagObject);

        for (int i = 0; i < mPresenters.size(); i++) {
            String fieldName = mPresenters.get(i);

            method.beginControlFlow("if ($N.$N != null)", target, fieldName)
                    .addStatement("$N.$N.unbind()", target, fieldName)
                    .endControlFlow();
            method.addStatement("$N.$N = null", target, fieldName);
        }

        method.addStatement("return null");
        method.endControlFlow().beginControlFlow("else");

        method.beginControlFlow("if (!($N instanceof $T))", tagObject, mPresenterDataType)
                .addStatement("throw new $T($S)", IllegalStateException.class, "Incorrect type of tag object.")
                .endControlFlow();

        method.addStatement("$T $N = ($T) $N", mPresenterDataType, param, mPresenterDataType, tagObject);

        for (int i = 0; i < mPresenters.size(); i++) {
            String fieldName = mPresenters.get(i);
            TypeName fieldType = mPresenterTypes.get(i);

            method.addStatement("$N.$N = ($T) $T.get($N, $N, $T.class)", target, fieldName, fieldType, PresenterManager.class, target, tagObject, fieldType);
            method.beginControlFlow("if ($N.$N == null)", target, fieldName)
                    .addStatement("$N.$N = new $T()", target, fieldName, fieldType)
                    .addStatement("$T.put($N, $N, $N.$N)", PresenterManager.class, target, tagObject, target, fieldName)
                    .addStatement("$N.$N.create($N)", target, fieldName, param)
                    .endControlFlow();
            method.addStatement("$N.$N.bind($N)", target, fieldName, target);
        }
        method.addStatement("return $N.toString()", tagObject);
        method.endControlFlow();

        builder.getBuilder().addMethod(method.build());
    }

    private void addUnbindPresenterMethod(HelperClassBuilder builder) {
        String target = "target";

        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_UNBIND_PRESENTERS)
                .addAnnotation(
                        WeaveBuilder.into("onDetachedFromWindow")
                                .addStatement("%s.%s(this);", fullName(builder.getClassName()), METHOD_NAME_UNBIND_PRESENTERS)
                                .build()
                )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target);

        for (int i = 0; i < mPresenters.size(); i++) {
            String fieldName = mPresenters.get(i);

            method.beginControlFlow("if ($N.$N != null)", target, fieldName)
                    .addStatement("$N.$N.unbind()", target, fieldName)
                    .endControlFlow();
        }

        builder.getBuilder().addMethod(method.build());
    }

    private static TypeName getPresenterDataType(TypeElement presenterTypeElement) {
        TypeName interfaceTypeName = ProcessorUtils.getSuperType(presenterTypeElement, ClassName.get(IPresenter.class));
        if (interfaceTypeName != null) {
            ParameterizedTypeName ptn = (ParameterizedTypeName) interfaceTypeName;
            return ptn.typeArguments.get(DATA_ARG);
        } else {
            return null;
        }
    }

    private boolean hasDefaultConstructor(TypeElement presenterTypeElement) {
        for (Element e : presenterTypeElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) e;
                if (constructor.getParameters().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

}
