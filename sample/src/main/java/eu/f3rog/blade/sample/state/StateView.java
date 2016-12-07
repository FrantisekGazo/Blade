package eu.f3rog.blade.sample.state;

import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import blade.State;

public final class StateView
        extends View {

    @State
    int num;

    public StateView(Context context) {
        super(context);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.i("StateView", "onSaveInstanceState");
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        Log.i("StateView", "onRestoreInstanceState");
    }
}
