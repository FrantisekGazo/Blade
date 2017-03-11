package eu.f3rog.blade.sample.mvp.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import blade.Blade;
import blade.Extra;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;


/**
 * Class {@link ActorViewActivity}
 *
 * @author FrantisekGazo
 */
@Blade
public final class ActorViewActivity
        extends AppCompatActivity {

    @Extra
    long mActorId;
    @Extra
    String mActorName;

    @BindView(R.id.view_actor)
    View mDetailView;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mvp_activity_actor_view);
        ButterKnife.bind(this);

        setTitle(mActorName);

        mDetailView.setTag(mActorId);
    }
}
