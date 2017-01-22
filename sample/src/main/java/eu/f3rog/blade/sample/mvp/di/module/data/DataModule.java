package eu.f3rog.blade.sample.mvp.di.module.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.f3rog.blade.sample.mvp.di.module.data.api.ActorApi;
import eu.f3rog.blade.sample.mvp.service.DataService;
import eu.f3rog.blade.sample.mvp.service.ImageLoader;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
public class DataModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                // here you can add custom gson type adapters
                .create();
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Provides
    @Singleton
    public ImageLoader provideImageLoader(@NonNull Context appContext, @NonNull OkHttpClient httpClient) {
        return new PicassoImageLoader(appContext, httpClient);
    }

    @Provides
    @Singleton
    public ActorApi provideActorApi(@NonNull OkHttpClient httpClient, @NonNull final Gson gson) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ActorApi.ENDPOINT)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(ActorApi.class);
    }

    @Provides
    @Singleton
    public DataService providesDataService(@NonNull final ActorApi api) {
        return new FakeDataService();
//        return new OnlineDataService(api);
    }
}
