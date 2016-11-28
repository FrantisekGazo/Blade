package eu.f3rog.blade.sample.mvp;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.presenter.DataPresenter;
import eu.f3rog.blade.sample.mvp.ui.view.IDataView;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Class {@link MyPresenterTest}
 *
 * @author FrantisekGazo
 * @version 2016-02-20
 */
public class MyPresenterTest {

    private static class MockDataView extends MockView implements IDataView {

        private String mShownValue;

        @Override
        public void showValue(String value) {
            System.out.println("Value is: " + value);
            mShownValue = value;
        }

        @Override
        public void showProgress() {
            mShownValue = null;
        }

    }

    @Before
    public void setup() {
    }

    @Test
    public void test() throws InterruptedException {
        DataPresenter presenter = new DataPresenter(Schedulers.immediate(), Schedulers.immediate());
        MockDataView view = new MockDataView();

        presenter.onViewCreated(new Data(123, 2, 1, "Hello World!"));
        presenter.onBind(view);

        Observable
                .timer(5, TimeUnit.SECONDS)
                .toBlocking()
                .last();    // Wait for observable to complete. Last item discarded.

        Assert.assertTrue("Hello World!".equals(view.mShownValue));
    }

}
