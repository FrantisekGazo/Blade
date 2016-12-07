package eu.f3rog.blade.sample.mvp.ui.view;

import blade.mvp.IView;


/**
 * Interface {@link IDataView}
 *
 * @author FrantisekGazo
 */
public interface IDataView extends IView {

    void showValue(String value);

    void showProgress();

}

