package eu.f3rog.blade.sample.mvp.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import javax.inject.Inject;

import blade.Blade;
import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.di.component.Component;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.presenter.DataPresenter;
import eu.f3rog.blade.sample.mvp.ui.view.IDataView;


/**
 * Class {@link TestMvpActivity}
 *
 * @author FrantisekGazo
 */
@Blade
public final class TestMvpActivity
        extends AppCompatActivity
        implements IDataView {

    @Inject
    DataPresenter mPresenter;

    @Bind(R.id.txt_activity_value)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mvp);
        ButterKnife.bind(this);

        Component.forApp().inject(this);

        mPresenter.onViewCreated(new Data(123, 10, 1, "Hello"));
    }

    @Override
    public void showValue(String value) {
        mTextView.setText(value);
    }

    @Override
    public void showProgress() {
        mTextView.setText("...");
    }

}
