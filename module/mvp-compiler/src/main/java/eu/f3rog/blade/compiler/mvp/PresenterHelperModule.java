package eu.f3rog.blade.compiler.mvp;

import android.view.View;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import blade.mvp.IPresenter;
import blade.mvp.IView;
import eu.f3rog.blade.compiler.builder.helper.BaseHelperModule;
import eu.f3rog.blade.compiler.builder.helper.HelperClassBuilder;
import eu.f3rog.blade.compiler.util.ProcessorError;
import eu.f3rog.blade.mvp.WeavedMvpActivity;
import eu.f3rog.blade.mvp.WeavedMvpFragment;
import eu.f3rog.blade.mvp.WeavedMvpView;

import static eu.f3rog.blade.compiler.util.ProcessorUtils.getSuperType;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.getTypeElement;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.isActivitySubClass;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.isFragmentSubClass;
import static eu.f3rog.blade.compiler.util.ProcessorUtils.isSubClassOf;

/**
 * Class {@link PresenterHelperModule}
 *
 * @author FrantisekGazo
 */
public final class PresenterHelperModule
        extends BaseHelperModule {

    private enum ViewType {
        ACTIVITY, FRAGMENT, VIEW
    }

    private ViewType mViewType;
    private final List<String> mPresenterFieldNames = new ArrayList<>();

    @Override
    public void checkClass(TypeElement e) throws ProcessorError {
        ViewType viewType = null;

        if (isSubClassOf(e, IView.class)) {
            if (isActivitySubClass(e)) {
                viewType = ViewType.ACTIVITY;
            } else if (isFragmentSubClass(e)) {
                viewType = ViewType.FRAGMENT;
            } else if (isSubClassOf(e, View.class)) {
                viewType = ViewType.VIEW;
            }
        }

        mViewType = viewType;
    }

    @Override
    public void add(VariableElement e) throws ProcessorError {
        TypeName presenterType = getSuperType(getTypeElement(ClassName.get(e.asType())), IPresenter.class);
        if (presenterType != null) {
            mPresenterFieldNames.add(e.getSimpleName().toString());

            if (mViewType == null) {
                throw new ProcessorError(e, MvpErrorMsg.Invalid_class_with_injected_Presenter);
            }

            // check if view type is the same as the one presenter needs
            TypeElement typeElement = (TypeElement) e.getEnclosingElement();
            ParameterizedTypeName paramPresenterType = (ParameterizedTypeName) presenterType;
            List<TypeName> typeArguments = paramPresenterType.typeArguments;
            TypeName presenterViewType = typeArguments.get(0);
            if (!isSubClassOf(typeElement, presenterViewType)) {
                throw new ProcessorError(e, String.format(MvpErrorMsg.Invalid_view_class, presenterViewType, e.getSimpleName()));
            }
        }
    }

    @Override
    public boolean implement(HelperClassBuilder builder) throws ProcessorError {
        if (mViewType == null) {
            return false; // if class is NOT a IView and does NOT inject any IPresenter
        }

        if (mViewType != ViewType.ACTIVITY && mPresenterFieldNames.isEmpty()) {
            return false; // if class is a IView but does NOT inject any IPresenter
        }

        // if class is a IView and inject some IPresenter

        switch (mViewType) {
            case ACTIVITY:
                builder.getBuilder().addSuperinterface(ClassName.get(WeavedMvpActivity.class));
                return true;
            case FRAGMENT:
                builder.getBuilder().addSuperinterface(ClassName.get(WeavedMvpFragment.class));
                return true;
            case VIEW:
                builder.getBuilder().addSuperinterface(ClassName.get(WeavedMvpView.class));
                return true;
            default:
                return false;
        }
    }

}
