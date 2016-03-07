package eu.f3rog.blade.sample.mvp.view;

import blade.mvp.IView;

/**
 * Interface {@link IDataView}
 *
 * @author FrantisekGazo
 * @version 2016-02-13
 */
public interface IDataView extends IView {

    void showValue(String value);

    void showProgress();

}

