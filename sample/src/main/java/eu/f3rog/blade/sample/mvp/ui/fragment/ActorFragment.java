package eu.f3rog.blade.sample.mvp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import blade.Arg;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.di.component.Component;
import eu.f3rog.blade.sample.mvp.model.ActorDetail;
import eu.f3rog.blade.sample.mvp.presenter.ActorPresenter;
import eu.f3rog.blade.sample.mvp.view.ActorView;

/**
 * Class {@link ActorFragment}
 *
 * @author FrantisekGazo
 */
public final class ActorFragment
        extends Fragment
        implements ActorView {

    @Arg
    long mId;

    @Inject
    ActorPresenter mPresenter;

    @BindView(android.R.id.text1)
    TextView mTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mvp_frag_actor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Component.forApp().inject(this);
        mPresenter.setActorId(mId);
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
