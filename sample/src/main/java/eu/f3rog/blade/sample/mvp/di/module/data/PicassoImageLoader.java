package eu.f3rog.blade.sample.mvp.di.module.data;


import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.ImageView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import eu.f3rog.blade.sample.mvp.service.ImageLoader;
import okhttp3.OkHttpClient;

/* package */ class PicassoImageLoader implements ImageLoader {

    @NonNull
    private final Context mAppContext;

    public PicassoImageLoader(@NonNull final Context appContext,
                              @NonNull final OkHttpClient httpClient) {

        mAppContext = appContext;
        Picasso.Builder builder = new Picasso.Builder(appContext)
                .downloader(new OkHttp3Downloader(httpClient));
        Picasso.setSingletonInstance(builder.build());
    }

    @Override
    public void load(@NonNull final String url, @NonNull final ImageView into) {
        Picasso.with(mAppContext).load(url).into(into);
    }
}
