package eu.f3rog.blade.sample.mvp.service;


import androidx.annotation.NonNull;
import android.widget.ImageView;

public interface ImageLoader {

    void load(@NonNull String url, @NonNull ImageView into);
}
