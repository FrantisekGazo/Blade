package eu.f3rog.blade.sample.arg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import java.util.Random;

import blade.Blade;
import blade.F;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.f3rog.blade.sample.R;

@Blade
public class TestArgActivity extends AppCompatActivity {

    @Bind(R.id.edt_num)
    EditText mEditNumber;
    @Bind(R.id.edt_text)
    EditText mEditText;

    @OnClick(R.id.btn_show_fragment)
    public void showFragment() {
        int number = Integer.parseInt(mEditNumber.getText().toString());
        String text = mEditText.getText().toString();
        Data data = new Data(number, text);

        double randomDouble = new Random().nextDouble();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frag, F.newDataFragment(randomDouble, data))
                .commit();
    }

    @OnClick(R.id.btn_show_dialog)
    public void b2() {
        F.newSampleDialogFragment("Hello mate :)").show(getSupportFragmentManager(), "some-tag");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_arg);
        ButterKnife.bind(this);
    }

}
