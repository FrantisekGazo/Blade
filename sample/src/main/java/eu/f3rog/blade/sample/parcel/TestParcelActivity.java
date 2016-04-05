package eu.f3rog.blade.sample.parcel;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Random;

import blade.Blade;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.f3rog.blade.sample.R;

@Blade
public class TestParcelActivity extends AppCompatActivity {

    @Bind(R.id.txt_output)
    TextView mOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_parcel);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_run_test)
    public void test() {
        Random rand = new Random();
        mOutput.setText("Testing:");

        Data data = new Data();
        data.fnum = rand.nextFloat();
        data.inum = rand.nextInt(100);
        test(data);

        test(new Data2());

        Data2 data2 = new Data2();
        data2.d = data;
        data2.number = rand.nextInt(100);
        data2.flag = rand.nextBoolean();
        data2.text = "<some-text-" + data.inum + ">";
        data2.array = new double[rand.nextInt(5)];
        for (int i = 0; i < data2.array.length; i++) {
            data2.array[i] = rand.nextDouble();
        }
        test(data2);

        SubData2 data3 = new SubData2();
        data3.d = data;
        data3.number = rand.nextInt(100);
        data3.flag = rand.nextBoolean();
        data3.text = "<some-text-" + data.inum + ">";
        data3.array = new double[rand.nextInt(5)];
        for (int i = 0; i < data3.array.length; i++) {
            data3.array[i] = rand.nextDouble();
        }
        data3.stringArray = new String[]{"abc", "def"};
        test(data3);
    }

    private void test(final Parcelable obj) {
        mOutput.append("\n> Created new object " + obj);

        final Parcel p1 = Parcel.obtain();
        p1.writeParcelable(obj, 0);
        final byte[] bytes = p1.marshall();
        p1.recycle();
        mOutput.append("\n> put in Bundle");

        final Parcel p2 = Parcel.obtain();
        p2.unmarshall(bytes, 0, bytes.length);
        p2.setDataPosition(0);
        final Parcelable result = p2.readParcelable(obj.getClass().getClassLoader());
        p2.recycle();
        mOutput.append("\n> retrieved from Bundle -> object " + result);

        mOutput.append("\n> Test ");
        mOutput.append(obj.equals(result) ? "was SUCCESSFULL" : "FAILED");

        mOutput.append("\n");
    }

}
