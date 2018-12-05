package eu.f3rog.blade.sample.extra;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import blade.Extra;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;

public class ExtraTextActivity extends AppCompatActivity {

    @Extra
    String mShowText;

    @BindView(R.id.txt)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_text);
        ButterKnife.bind(this);

        mTextView.setText(mShowText);
    }

}
