package eu.f3rog.blade.sample.extra;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;

import blade.Blade;
import blade.I;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.f3rog.blade.sample.R;

@Blade
public class TestExtraActivity extends AppCompatActivity {

    @BindView(R.id.edt)
    EditText mEditText;

    @OnClick(R.id.btn_show_text)
    public void showTextActivity() {
        I.startExtraTextActivity(this, mEditText.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_extra);
        ButterKnife.bind(this);
    }

}
