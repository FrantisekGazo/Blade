package eu.f3rog.blade.sample.mvp.presenter;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import blade.State;
import blade.mvp.BasePresenter;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.ui.view.IDataView;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Class {@link DataPresenter}
 *
 * @author FrantisekGazo
 */
public class DataPresenter extends BasePresenter<IDataView> {

    private final Scheduler mainScheduler;
    private final Scheduler taskScheduler;
    private Data mData;
    @State
    String mLoadedValue;

    public DataPresenter(Scheduler mainScheduler, Scheduler taskScheduler) {
        this.mainScheduler = mainScheduler;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void onBind(@NonNull IDataView view) {
        super.onBind(view);

        if (mLoadedValue != null) {
            view.showValue(mLoadedValue);
        } else {
            view.showProgress();
        }
    }

    private void startFakeLoading() {
        mLoadedValue = null;
        Observable.range(0, mData.getCount() + 1)
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
                        return integer < mData.getCount() ? String.valueOf(mData.getCount() - integer) : mData.getText();
                    }
                })
                .subscribeOn(taskScheduler)
                .observeOn(mainScheduler)
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

    public void onViewCreated(Data data) {
        if (mData == null) {
            mData = data;

            startFakeLoading();
        }
    }
}
