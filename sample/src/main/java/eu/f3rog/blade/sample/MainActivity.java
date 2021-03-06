package eu.f3rog.blade.sample;

import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.app.AppCompatActivity;

import blade.I;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @OnClick(R.id.btn_go_extra)
    public void gotoExtra() {
        I.startTestExtraActivity(this);
    }

    @OnClick(R.id.btn_go_arg)
    public void gotoArg() {
        I.startTestArgActivity(this);
    }

    @OnClick(R.id.btn_go_state)
    public void gotoState() {
        I.startTestStateActivity(this);
    }

    @OnClick(R.id.btn_go_mvp)
    public void gotoMvp() {
        I.startMvpMainActivity(this);
    }

    @OnClick(R.id.btn_go_parcel)
    public void gotoParcel() {
        I.startTestParcelActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

}
