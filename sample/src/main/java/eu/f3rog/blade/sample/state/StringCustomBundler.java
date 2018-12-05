package eu.f3rog.blade.sample.state;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import blade.Bundler;

public final class StringCustomBundler implements Bundler<String> {

    @Override
    public void save(@Nullable String value, @NonNull Bundle state) {
        String v = value;
        if (v != null) {
            v += "a";
        } else {
            v = "a";
        }
        state.putString("some_key", v);
    }

    @Nullable
    @Override
    public String restore(@NonNull Bundle state) {
        return state.getString("some_key");
    }
}
