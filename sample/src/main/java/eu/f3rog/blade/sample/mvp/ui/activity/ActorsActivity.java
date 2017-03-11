package eu.f3rog.blade.sample.mvp.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import blade.Blade;
import blade.Extra;
import blade.F;
import blade.I;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;
import eu.f3rog.blade.sample.mvp.di.component.Component;
import eu.f3rog.blade.sample.mvp.model.Actor;
import eu.f3rog.blade.sample.mvp.presenter.ActorListPresenter;
import eu.f3rog.blade.sample.mvp.ui.adapter.ActorAdapter;
import eu.f3rog.blade.sample.mvp.view.ActorListView;


/**
 * Class {@link ActorsActivity}
 *
 * @author FrantisekGazo
 */
@Blade
public final class ActorsActivity
        extends AppCompatActivity
        implements ActorListView, ActorAdapter.OnActorClickListener {

    public enum DetailType {
        DIALOG_FRAG, ACTIVITY_WITH_FRAG, ACTIVITY_WITH_VIEW
    }

    @Extra
    DetailType mDetailType;

    @BindView(android.R.id.text1)
    TextView mText;
    @BindView(R.id.rv_actors)
    RecyclerView mRecyclerView;

    @Inject
    ActorListPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mvp_activity_actors);
        ButterKnife.bind(this);
        setTitle("Actors");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Component.forApp().inject(this);
    }

    @Override
    public void showProgress() {
        mText.setText("Loading...");

        mRecyclerView.setVisibility(View.GONE);
        mText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(@NonNull final String errorMessage) {
        mText.setText("Error: " + errorMessage);

        mRecyclerView.setVisibility(View.GONE);
        mText.setVisibility(View.VISIBLE);
    }

    @Override
    public void show(@NonNull final List<Actor> actors) {
        final ActorAdapter adapter = new ActorAdapter(this, actors, this);
        mRecyclerView.setAdapter(adapter);

        mText.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void gotoActorDetail(@NonNull final Actor actor) {
        switch (mDetailType) {
            case DIALOG_FRAG:
                F.newActorDialogFragment(actor.getId(), actor.getName()).show(getSupportFragmentManager(), "dialog-tag");
                break;
            case ACTIVITY_WITH_FRAG:
                I.startActorFragmentActivity(this, actor.getId(), actor.getName());
                break;
            case ACTIVITY_WITH_VIEW:
                I.startActorViewActivity(this, actor.getId(), actor.getName());
                break;
        }
    }

    @Override
    public void onClick(@NonNull final Actor actor) {
        mPresenter.onActorSelected(actor);
    }
}
