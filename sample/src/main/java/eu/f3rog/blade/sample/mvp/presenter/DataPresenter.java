package eu.f3rog.blade.sample.mvp.presenter;

import android.os.AsyncTask;

import java.util.concurrent.TimeUnit;

import blade.mvp.BasePresenter;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.view.IDataView;

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
        fakeLoadInBackground();
    }

    @Override
    public <T extends IDataView> void bind(T view) {
        super.bind(view);

        if (mLoadedValue != null) {
            view.showValue(mLoadedValue);
        } else {
            view.showProgress();
        }
    }

    private void fakeLoadInBackground() {
        Loader loader = new Loader();
        loader.execute(mData);
    }

    private class Loader extends AsyncTask<Data, Void, String> {

        @Override
        protected String doInBackground(Data... params) {
            Data data = params[0];

            // fake loading
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(data.getWait()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return data.getText();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mLoadedValue = s;

            // show result
            if (getView() != null) {
                getView().showValue(mLoadedValue);
            }
        }

    }

}
