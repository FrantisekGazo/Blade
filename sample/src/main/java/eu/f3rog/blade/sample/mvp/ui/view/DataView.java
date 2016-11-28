package eu.f3rog.blade.sample.mvp.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.presenter.DataPresenter;


/**
 * Class {@link DataView}
 *
 * @author FrantisekGazo
 */
public final class DataView
        extends LinearLayout
        implements IDataView {

    @Nullable
    @Bind(R.id.value_layout)
    View mValueLayout;
    @Nullable
    @Bind(R.id.txt_value)
    TextView mTxtValue;
    @Nullable
    @Bind(R.id.progress)
    ProgressBar mProgressBar;

    // FIXME @Inject
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

//        mValueLayout.setVisibility(GONE);
//        mProgressBar.setVisibility(GONE);

        // random data (normally they would be send via constructor from activity)
        // FIXME : set to presenter Data data = new Data(123, 5, 1, "Loaded Text");
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

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        return parcelable;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
