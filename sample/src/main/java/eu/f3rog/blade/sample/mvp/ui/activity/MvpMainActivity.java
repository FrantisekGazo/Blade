package eu.f3rog.blade.sample.mvp.ui.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import blade.Blade;
import blade.I;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.f3rog.blade.sample.R;


@Blade
public final class MvpMainActivity
        extends AppCompatActivity {

    @OnClick(R.id.btn_1)
    void showExample1() {
        I.startActorsActivity(this, ActorsActivity.DetailType.DIALOG_FRAG);
    }

    @OnClick(R.id.btn_2)
    void showExample2() {
        I.startActorsActivity(this, ActorsActivity.DetailType.ACTIVITY_WITH_FRAG);
    }

    @OnClick(R.id.btn_3)
    void showExample3() {
        I.startActorsActivity(this, ActorsActivity.DetailType.ACTIVITY_WITH_VIEW);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mvp_activity_main);
        ButterKnife.bind(this);
    }
}
