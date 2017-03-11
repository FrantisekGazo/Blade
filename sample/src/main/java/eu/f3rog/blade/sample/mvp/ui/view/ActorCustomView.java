package eu.f3rog.blade.sample.mvp.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.di.component.Component;
import eu.f3rog.blade.sample.mvp.model.ActorDetail;
import eu.f3rog.blade.sample.mvp.presenter.ActorPresenter;
import eu.f3rog.blade.sample.mvp.view.ActorView;


/**
 * Class {@link ActorCustomView}
 *
 * @author FrantisekGazo
 */
public final class ActorCustomView
        extends LinearLayout
        implements ActorView {

    @BindView(android.R.id.text1)
    TextView mText;
    @BindView(R.id.progress)
    ProgressBar mProgressBar;

    @Inject
    ActorPresenter mPresenter;

    public ActorCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActorCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ActorCustomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        mText.setVisibility(GONE);
        mProgressBar.setVisibility(GONE);
    }

    @Override
    public void showProgress() {
        mText.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
    }

    @Override
    public void showError(@NonNull final String errorMessage) {
        mText.setText(errorMessage);

        mText.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
    }

    @Override
    public void show(@NonNull final ActorDetail actorDetail) {
        mText.setText(actorDetail.getBio());

        mText.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Component.forApp().inject(this);
        mPresenter.setActorId((Long) getTag());
    }
}
