package eu.f3rog.blade.sample.arg;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

import blade.Blade;
import blade.F;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.f3rog.blade.sample.R;

@Blade
public class TestArgActivity extends AppCompatActivity {

    @BindView(R.id.edt_num)
    EditText mEditNumber;
    @BindView(R.id.edt_text)
    EditText mEditText;

    @OnClick(R.id.btn_show_fragment)
    public void showFragment() {
        if (mEditNumber.getText().length() == 0) {
            Toast.makeText(this, "Input a number!", Toast.LENGTH_SHORT).show();
            mEditNumber.requestFocus();
            return;
        }
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
