package eu.f3rog.blade.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import blade.Arg;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Class {@link DataFragment}
 *
 * @author FrantisekGazo
 * @version 2015-11-28
 */
public class DataFragment extends BaseFragment {

    @Arg
    Data data;

    @Bind(android.R.id.text1)
    TextView mTextViewNumber;
    @Bind(android.R.id.text2)
    TextView mTextViewText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mTextViewNumber.setText(String.valueOf(data.getNumber()));
        mTextViewText.setText(data.getText() + number);
    }

}