package eu.f3rog.blade.sample.mvp.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import blade.Arg;
import blade.Blade;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.di.component.Component;
import eu.f3rog.blade.sample.mvp.model.ActorDetail;
import eu.f3rog.blade.sample.mvp.presenter.ActorPresenter;
import eu.f3rog.blade.sample.mvp.view.ActorView;

/**
 * Class {@link ActorDialogFragment}
 *
 * @author FrantisekGazo
 */
@Blade
public final class ActorDialogFragment
        extends DialogFragment
        implements ActorView {

    @Arg
    long mId;
    @Arg
    String mName;

    @Inject
    ActorPresenter mPresenter;

    @BindView(android.R.id.text1)
    TextView mTextView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.mvp_frag_actor, null, false);
        ButterKnife.bind(this, view);

        Component.forApp().inject(this);
        mPresenter.setActorId(mId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(mName)
                .setView(view);

        return builder.create();
    }

    @Override
    public void showProgress() {
        mTextView.setText("Loading...");
    }

    @Override
    public void showError(@NonNull final String errorMessage) {
        mTextView.setText("Error: " + errorMessage);
    }

    @Override
    public void show(@NonNull final ActorDetail actorDetail) {
        mTextView.setText(actorDetail.toString());
    }
}
