package eu.f3rog.blade.sample.mvp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import blade.Blade;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;

@Blade
public class TestMvpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mvp);
        ButterKnife.bind(this);
    }

}
