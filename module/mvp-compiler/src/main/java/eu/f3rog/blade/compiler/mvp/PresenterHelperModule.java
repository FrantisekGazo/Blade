package eu.f3rog.blade.compiler.mvp;

import android.view.View;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
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
import eu.f3rog.blade.compiler.builder.annotation.WeaveBuilder;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.compiler.util.ProcessorUtils;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.cannotHaveAnnotation;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.fullName;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.getTypeElement;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.isActivitySubClass;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.isSubClassOf;

/**
 * Class {@link PresenterHelperModule}
 *
 * @author FrantisekGazo
 * @version 2015-12-15
 */
public class PresenterHelperModule extends BaseHelperModule {

    public static final String TARGET = "target";
    public static final String IF_N_N_NULL = "if ($N.$N != null)";
    public static final String S_S_THIS = "%s.%s(this);";

    private enum ViewType {
        ACTIVITY, VIEW
    }

    private static final String FIELD_NAME_IS_ATTACHED = "mIsAttached";
    private static final String METHOD_NAME_CREATE_PRESENTERS = "setPresenters";
    private static final String METHOD_NAME_UNBIND_PRESENTERS = "unbindPresenters";
    private static final String METHOD_NAME_BIND_PRESENTERS = "bindPresenters";

    private static final int DATA_ARG = 1;

    private ViewType mViewType = null;
    private TypeName mPresenterDataType = null;
    private List<String> mPresenters = new ArrayList<>();
    private List<TypeName> mPresenterTypes = new ArrayList<>();

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        ViewType viewType = null;

        if (isSubClassOf(e, IView.class)) {
            if (isActivitySubClass(e)) {
                viewType = ViewType.ACTIVITY;
            } else if (isSubClassOf(e, View.class)) {
                viewType = ViewType.VIEW;
            }
        }

        if (viewType == null) {
            throw new ProcessorError(e, MvpErrorMsg.Invalid_class_with_Presenter);
        }

        mViewType = viewType;
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
    public boolean implement(ProcessingEnvironment processingEnvironment, HelperClassBuilder builder) throws ProcessorError {
        switch (mViewType) {
            case VIEW:
                addIsAttachedField(builder);
            case ACTIVITY:
                addSetPresenterMethod(builder);
                addBindPresenterMethod(builder);
                addUnbindPresenterMethod(builder);
                return true;
            default:
                return false;
        }
    }

    private void addSetPresenterMethod(HelperClassBuilder builder) {
        String tagObject = "tagObject";
        String target = TARGET;

        boolean returnsString = (mViewType == ViewType.VIEW);

        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_CREATE_PRESENTERS)
                .addAnnotation(
                        weaveSetPresenters(builder)
                )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target)
                .addParameter(Object.class, tagObject);

        if (returnsString) {
            method.returns(String.class);
        }

        String param = "param";

        method.beginControlFlow("if ($N == null)", tagObject);

        for (int i = 0; i < mPresenters.size(); i++) {
            String fieldName = mPresenters.get(i);

            method.beginControlFlow(IF_N_N_NULL, target, fieldName)
                    .addStatement("$N.$N.unbind()", target, fieldName)
                    .endControlFlow();
            method.addStatement("$N.$N = null", target, fieldName);
        }

        if (returnsString) {
            method.addStatement("return null");
        }
        method.endControlFlow().beginControlFlow("else");

        method.beginControlFlow("if (!($N instanceof $T))", tagObject, mPresenterDataType)
                .addStatement("throw new $T($S)", IllegalStateException.class, "Incorrect type of tag object.")
                .endControlFlow();

        method.addStatement("$T $N = ($T) $N", mPresenterDataType, param, mPresenterDataType, tagObject);

        for (int i = 0; i < mPresenters.size(); i++) {
            String fieldName = mPresenters.get(i);
            TypeName fieldType = mPresenterTypes.get(i);

            method.addStatement("$N.$N = ($T) $T.get($N, $N, $T.class)", target, fieldName, fieldType, PresenterManager.class, target, param, fieldType);
            method.beginControlFlow("if ($N.$N == null)", target, fieldName)
                    .addStatement("$N.$N = new $T()", target, fieldName, fieldType)
                    .addStatement("$T.put($N, $N, $N.$N)", PresenterManager.class, target, param, target, fieldName)
                    .endControlFlow();
        }
        if (returnsString) {
            method.addStatement("return $N.toString()", tagObject);
        }
        method.endControlFlow();

        builder.getBuilder().addMethod(method.build());
    }

    private AnnotationSpec weaveSetPresenters(HelperClassBuilder builder) {
        String helperClassName = fullName(builder.getClassName());
        switch (mViewType) {
            case VIEW:
                return WeaveBuilder.weave().method("setTag", Object.class)
                        .withStatement("String tag = %s.%s(this, $1);",
                                helperClassName, METHOD_NAME_CREATE_PRESENTERS)
                        .withStatement(" super.setTag(tag);")
                        .withStatement(" if (this.%s) { %s.%s(this); }",
                                FIELD_NAME_IS_ATTACHED, helperClassName, METHOD_NAME_BIND_PRESENTERS)
                        .withStatement(" return;")
                        .build();
            case ACTIVITY:
                return WeaveBuilder.weave().method("setTag", Object.class)
                        .withStatement("%s.%s(this, $1);",
                                helperClassName, METHOD_NAME_CREATE_PRESENTERS)
                        .withStatement(" %s.%s(this);",
                                helperClassName, METHOD_NAME_BIND_PRESENTERS)
                        .build();
            default:
                return null;
        }
    }

    private void addIsAttachedField(HelperClassBuilder builder) {
        FieldSpec.Builder field = FieldSpec.builder(boolean.class, FIELD_NAME_IS_ATTACHED, Modifier.PRIVATE)
                .addAnnotation(
                        WeaveBuilder.weave().field().build()
                );

        builder.getBuilder().addField(field.build());
    }

    private void addBindPresenterMethod(HelperClassBuilder builder) {
        String target = TARGET;

        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_BIND_PRESENTERS)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target);

        if (mViewType == ViewType.VIEW) {
            method.addAnnotation(
                    WeaveBuilder.weave().method("onAttachedToWindow")
                            .withStatement(S_S_THIS, fullName(builder.getClassName()), METHOD_NAME_BIND_PRESENTERS)
                            .withStatement(" this.%s = true;", FIELD_NAME_IS_ATTACHED)
                            .build()
            );
        }

        for (int i = 0; i < mPresenters.size(); i++) {
            String fieldName = mPresenters.get(i);

            method.beginControlFlow(IF_N_N_NULL, target, fieldName)
                    .addStatement("$N.$N.bind($N)", target, fieldName, target)
                    .endControlFlow();
        }

        builder.getBuilder().addMethod(method.build());
    }

    private void addUnbindPresenterMethod(HelperClassBuilder builder) {
        String target = TARGET;

        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_UNBIND_PRESENTERS)
                .addAnnotation(
                        weaveUnbindPresenters(builder)
                )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(builder.getArgClassName(), target);

        for (int i = 0; i < mPresenters.size(); i++) {
            String fieldName = mPresenters.get(i);

            method.beginControlFlow(IF_N_N_NULL, target, fieldName)
                    .addStatement("$N.$N.unbind()", target, fieldName)
                    .endControlFlow();
        }

        builder.getBuilder().addMethod(method.build());
    }

    private AnnotationSpec weaveUnbindPresenters(HelperClassBuilder builder) {
        switch (mViewType) {
            case VIEW:
                return WeaveBuilder.weave().method("onDetachedFromWindow")
                        .withStatement(S_S_THIS, fullName(builder.getClassName()), METHOD_NAME_UNBIND_PRESENTERS)
                        .withStatement(" this.%s = false;", FIELD_NAME_IS_ATTACHED)
                        .build();
            case ACTIVITY:
                return WeaveBuilder.weave().method("onDestroy")
                        .withStatement(S_S_THIS, fullName(builder.getClassName()), METHOD_NAME_UNBIND_PRESENTERS)
                        .build();
            default:
                return null;
        }
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

    private static boolean hasDefaultConstructor(TypeElement presenterTypeElement) {
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
