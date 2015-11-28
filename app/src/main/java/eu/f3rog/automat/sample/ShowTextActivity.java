package eu.f3rog.automat.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.TextView;

import automat.F;
import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.automat.Extra;

public class ShowTextActivity extends AppCompatActivity {

    @Extra
    String mInitText;

    @Bind(android.R.id.text1)
    TextView mShownText;
    @Bind(R.id.frag)
    FrameLayout mFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_text);
        ButterKnife.bind(this);
        mShownText.setText(mInitText);

        DataFragment frag = F.newDataFragment(new Data(13, "Hello World!"));
        getSupportFragmentManager().beginTransaction().add(R.id.frag, frag).commit();
    }
}
