package eu.f3rog.blade.sample.arg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Sample Dialog")
                .setMessage(mMessage);

        return builder.create();
    }

}
