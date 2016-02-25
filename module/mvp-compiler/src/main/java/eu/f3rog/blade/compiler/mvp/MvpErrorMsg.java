package eu.f3rog.blade.compiler.mvp;

import blade.Presenter;
import blade.mvp.IPresenter;
import blade.mvp.IView;
import eu.f3rog.blade.compiler.ErrorMsg;

/**
 * Enum {@link MvpErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public interface MvpErrorMsg extends ErrorMsg {

    String Invalid_class_with_Presenter = "Only View subclass that implements " + IView.class.getCanonicalName() + " can contain @" + Presenter.class.getSimpleName() + ".";
    String Invalid_Presenter_class = "@" + Presenter.class.getSimpleName() + " has to be non-abstract class that implements " + IPresenter.class.getCanonicalName();
    String Inconsistent_Presenter_parameter_classes = "All @" + Presenter.class.getSimpleName() + "s has to implement " + IPresenter.class.getCanonicalName() + " with the same parameter type.";
    String Presenter_class_cannot_be_parametrized = "@" + Presenter.class.getSimpleName() + " class cannot e parametrized.";
    String Presenter_class_missing_default_constructor = "Presenter class has to contain default constructor.";

}
