package eu.f3rog.blade.sample.state;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import blade.Blade;
import blade.State;
import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;

@Blade
public class TestStateActivity extends AppCompatActivity {

    @State
    int mCounts;

    @Bind(R.id.txt)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_state);
        ButterKnife.bind(this);

        mTextView.setText(String.format("Rotation count: %d", mCounts));
        mCounts++;
    }

}
