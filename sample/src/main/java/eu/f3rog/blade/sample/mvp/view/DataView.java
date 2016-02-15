package eu.f3rog.blade.sample.mvp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import blade.Presenter;
import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.presenter.DataPresenter;

/**
 * Class {@link DataView}
 *
 * @author FrantisekGazo
 * @version 2016-02-15
 */
public class DataView extends LinearLayout implements IDataView {

    @Bind(R.id.value_layout)
    View mValueLayout;
    @Bind(R.id.txt_value)
    TextView mTxtValue;
    @Bind(R.id.progress)
    ProgressBar mProgressBar;

    @Presenter
    DataPresenter mPresenter;

    public DataView(Context context) {
        super(context);
    }

    public DataView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DataView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        mValueLayout.setVisibility(GONE);
        mProgressBar.setVisibility(GONE);

        // random data (normally they would be send via constructor from activity)
        Data data = new Data(5, "Loaded Text");

        setTag(data);
    }

    @Override
    public void showProgress() {
        mValueLayout.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
    }

    @Override
    public void showValue(String value) {
        mTxtValue.setText(value);

        mValueLayout.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
    }
}
