package eu.f3rog.blade.sample.parcel;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import blade.Blade;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.f3rog.blade.sample.R;

@Blade
public class TestParcelActivity extends AppCompatActivity {

    @BindView(R.id.txt_output)
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

        LinkedList<String> l = new LinkedList<>();
        l.add("Hello");
        l.add("World");
        final Parcel p1 = Parcel.obtain();
        p1.writeValue(l);
        p1.setDataPosition(0);
        List<String> l2 = (List<String>) p1.readValue(null);



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

        SubData2 subData2 = new SubData2();
        subData2.d = data;
        subData2.number = rand.nextInt(100);
        subData2.flag = rand.nextBoolean();
        subData2.text = "<some-text-" + data.inum + ">";
        subData2.array = new double[rand.nextInt(5)];
        for (int i = 0; i < subData2.array.length; i++) {
            subData2.array[i] = rand.nextDouble();
        }
        subData2.stringArray = new String[]{"abc", "def"};
        test(subData2);

        IgnoreData ignoreData = new IgnoreData();
        ignoreData.i = 123;
        ignoreData.d = 2.2d;
        test(ignoreData);

        ignoreData.f = 1.1f;
        test(ignoreData, false);


        Data3 data3 = new Data3();
        data3.g = new GenericData<>("Hello :)");
        test(data3);
    }

    private void test(final Parcelable obj) {
        test(obj, true);
    }

    private void test(final Parcelable obj, boolean equal) {
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

        mOutput.append("\n> Objects should be ");
        mOutput.append(equal ? "EQUAL" : "DIFFERENT");
        mOutput.append("\n> Test ");
        mOutput.append(equal == obj.equals(result) ? "was SUCCESSFULL" : "FAILED");

        mOutput.append("\n");
    }

}
