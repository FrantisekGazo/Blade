package eu.f3rog.automat.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import automat.F;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.f3rog.automat.Extra;

public class ShowTextActivity extends AppCompatActivity {

    @Extra
    String mInitText;

    @Bind(android.R.id.text1)
    TextView mShownText;

    @Bind(R.id.et_num)
    EditText mEditNumber;
    @Bind(R.id.et_text)
    EditText mEditText;

    @OnClick(R.id.show_fragment)
    public void showFragment() {
        Data data = new Data(Integer.parseInt(mEditNumber.getText().toString()), mEditText.getText().toString());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frag, F.newDataFragment(8d, data))
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_text);
        ButterKnife.bind(this);
        mShownText.setText(mInitText);
    }

}
