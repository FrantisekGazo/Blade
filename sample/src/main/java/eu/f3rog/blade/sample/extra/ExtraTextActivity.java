package eu.f3rog.blade.sample.extra;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import blade.Extra;
import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;

public class ExtraTextActivity extends AppCompatActivity {

    @Extra
    String mShowText;

    @Bind(R.id.txt)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_text);
        ButterKnife.bind(this);

        mTextView.setText(mShowText);
    }

}
