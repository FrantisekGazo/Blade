package eu.f3rog.automat.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import automat.I;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.edt)
    EditText mEditText;

    @OnClick(R.id.btn_1)
    public void b1() {
        I.startShowTextActivity(this, mEditText.getText().toString());
    }

    @OnClick(R.id.btn_2)
    public void b2() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
}
