package eu.f3rog.blade.sample.arg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import blade.Arg;

/**
 * Class {@link SampleDialogFragment}
 *
 * @author FrantisekGazo
 * @version 2016-01-07
 */
public class SampleDialogFragment extends DialogFragment {

    @Arg
    String mMessage;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Sample Dialog")
                .setMessage(mMessage);

        return builder.create();
    }

}
