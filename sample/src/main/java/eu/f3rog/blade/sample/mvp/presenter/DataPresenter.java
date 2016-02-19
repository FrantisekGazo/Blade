package eu.f3rog.blade.sample.mvp.presenter;

import java.util.concurrent.TimeUnit;

import blade.mvp.BasePresenter;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.view.IDataView;
import rx.Observable;
import rx.functions.Action1;

/**
 * Class {@link DataPresenter}
 *
 * @author FrantisekGazo
 * @version 2016-02-08
 */
public class DataPresenter extends BasePresenter<IDataView, Data> {

    private Data mData;
    private String mLoadedValue;

    @Override
    public void create(Data data) {
        super.create(data);

        mData = data;
        startFakeLoading();
    }

    @Override
    public void bind(IDataView view) {
        super.bind(view);

        if (mLoadedValue != null) {
            view.showValue(mLoadedValue);
        } else {
            view.showProgress();
        }
    }

    private void startFakeLoading() {
        mLoadedValue = null;
        Observable.just(mData.getText())
                .delay(mData.getWait(), TimeUnit.SECONDS)
                .subscribeOn(S.getBackgroundScheduler())
                .observeOn(S.getMainScheduler())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mLoadedValue = s;
                        // show result
                        if (getView() != null) {
                            getView().showValue(mLoadedValue);
                        }
                    }
                });
    }

}
