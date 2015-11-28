package eu.f3rog.automat.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.automat.Extra;

public class ShowTextActivity extends AppCompatActivity {

    @Extra
    String mInitText;

    @Bind(android.R.id.text1)
    TextView mShownText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_text);
        ButterKnife.bind(this);
        mShownText.setText(mInitText);
    }
}
