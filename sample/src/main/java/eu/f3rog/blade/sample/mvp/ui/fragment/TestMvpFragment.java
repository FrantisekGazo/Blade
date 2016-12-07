package eu.f3rog.blade.sample.mvp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.di.component.Component;
import eu.f3rog.blade.sample.mvp.model.Data;
import eu.f3rog.blade.sample.mvp.presenter.DataPresenter;
import eu.f3rog.blade.sample.mvp.ui.view.IDataView;

/**
 * Class {@link TestMvpFragment}
 *
 * @author FrantisekGazo
 */
public final class TestMvpFragment
        extends Fragment
        implements IDataView {

    @Inject
    DataPresenter mPresenter;

    @Bind(android.R.id.text1)
    TextView mTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test_mvp, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Component.forApp().inject(this);

        mPresenter.onViewCreated(new Data(123, 20, 1, "World!"));
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
