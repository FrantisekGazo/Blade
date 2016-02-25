package eu.f3rog.blade.sample.mvp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import blade.Blade;
import blade.Presenter;
import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.presenter.DataPresenter;
import eu.f3rog.blade.sample.mvp.view.IDataView;

@Blade
public class TestMvpActivity extends AppCompatActivity implements IDataView {

    @Presenter
    DataPresenter mPresenter;

    @Bind(R.id.txt_activity_value)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mvp);
        ButterKnife.bind(this);

        setTag(new Data(123, 1, "Hello World!"));
    }

    @Override
    public void setTag(Object o) {
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
