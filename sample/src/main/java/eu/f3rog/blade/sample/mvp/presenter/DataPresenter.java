package eu.f3rog.blade.sample.mvp.presenter;

import java.util.concurrent.TimeUnit;

import blade.mvp.BasePresenter;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.view.IDataView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Class {@link DataPresenter}
 *
 * @author FrantisekGazo
 * @version 2016-02-08
 */
public class DataPresenter extends BasePresenter<IDataView, Data> {

    private static final int COUNT = 10;
    private Data mData;
    private String mLoadedValue;

    @Override
    public void create(Data data, boolean wasRestored) {
        super.create(data, wasRestored);

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
        Observable.range(0, COUNT + 1)
                .delay(mData.getWait(), TimeUnit.SECONDS)
                .delay(new Func1<Integer, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Integer integer) {
                        return Observable.timer(integer, TimeUnit.SECONDS);
                    }
                })
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer integer) {
                        return integer < COUNT ? String.valueOf(COUNT - integer) : mData.getText();
                    }
                })
                .subscribeOn(S.getBackgroundScheduler())
                .observeOn(S.getMainScheduler())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        show(s);
                    }
                });
    }

    private void show(String value) {
        mLoadedValue = value;
        // show result
        if (getView() != null) {
            getView().showValue(mLoadedValue);
        }
    }

}
