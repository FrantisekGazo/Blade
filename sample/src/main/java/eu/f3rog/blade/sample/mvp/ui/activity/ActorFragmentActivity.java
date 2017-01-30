package eu.f3rog.blade.sample.mvp.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import blade.Blade;
import blade.Extra;
import blade.F;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;


/**
 * Class {@link ActorFragmentActivity}
 *
 * @author FrantisekGazo
 */
@Blade
public final class ActorFragmentActivity
        extends AppCompatActivity {

    @Extra
    long mActorId;
    @Extra
    String mActorName;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mvp_activity_actor_fragment);
        ButterKnife.bind(this);

        setTitle(mActorName);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frag, F.newActorFragment(mActorId))
                    .commit();
        }
    }
}
