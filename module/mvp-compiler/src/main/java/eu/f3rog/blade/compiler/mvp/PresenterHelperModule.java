package eu.f3rog.blade.compiler.mvp;

import com.squareup.javapoet.ClassName;

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
        ACTIVITY, FRAGMENT
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
            }

            // TODO : look at View subclass support
        }

        mViewType = viewType;
    }

    @Override
    public void add(VariableElement e) throws ProcessorError {
        if (isSubClassOf(getTypeElement(ClassName.get(e.asType())), IPresenter.class)) {
            mPresenterFieldNames.add(e.getSimpleName().toString());

            if (mViewType == null) {
                throw new ProcessorError(e, MvpErrorMsg.Invalid_class_with_injected_Presenter);
            }

            // FIXME : add check for view type vs presenter arg type
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
            default:
                return false;
        }
    }

}
