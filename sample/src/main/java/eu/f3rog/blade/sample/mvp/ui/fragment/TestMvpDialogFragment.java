package eu.f3rog.blade.sample.mvp.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
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
 * Class {@link TestMvpDialogFragment}
 *
 * @author FrantisekGazo
 */
@Blade
public final class TestMvpDialogFragment
        extends DialogFragment
        implements IDataView {

    @Inject
    DataPresenter mPresenter;

    @Bind(android.R.id.text1)
    TextView mTextView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_test_mvp, null, false);
        ButterKnife.bind(this, view);

        Component.forApp().inject(this);
        mPresenter.onViewCreated(new Data(1, 5, 1, "Done!"));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("MVP Dialog")
                .setView(view);

        return builder.create();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
