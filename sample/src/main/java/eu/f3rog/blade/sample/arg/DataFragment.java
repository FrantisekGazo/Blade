package eu.f3rog.blade.sample.arg;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import blade.Arg;
import butterknife.Bind;
import butterknife.ButterKnife;
import eu.f3rog.blade.sample.R;

/**
 * Class {@link DataFragment}
 *
 * @author FrantisekGazo
 * @version 2015-11-28
 */
public class DataFragment extends BaseFragment {

    @Arg
    Data data;

    @Bind(R.id.txt_value_int)
    TextView mTxtInteger;
    @Bind(R.id.txt_value_string)
    TextView mTxtString;
    @Bind(R.id.txt_value_double)
    TextView mTxtDouble;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mTxtInteger.setText(String.valueOf(data.getNumber()));
        mTxtString.setText(data.getText());
        mTxtDouble.setText(String.valueOf(number));
    }

}