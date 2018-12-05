package eu.f3rog.blade.sample.state;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import blade.Blade;
import blade.State;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;

@Blade
public class TestStateActivity extends AppCompatActivity {

    @State
    int mCounts;
    @State(StringCustomBundler.class)
    String mText;

    @BindView(R.id.txt)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_state);
        ButterKnife.bind(this);

        mTextView.setText(String.format("Rotation count: %d \n%s", mCounts, mText));
        mCounts++;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.e(TestStateActivity.class.getCanonicalName(), "exec some code before @State code");
        super.onSaveInstanceState(outState);
    }
}
