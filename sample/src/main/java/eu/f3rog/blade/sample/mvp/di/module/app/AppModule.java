package eu.f3rog.blade.sample.mvp.di.module.app;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    @NonNull
    private final Application mApp;

    public AppModule(@NonNull final Application app) {
        mApp = app;
    }

    @Provides
    public Application provideApp() {
        return mApp;
    }

    @Provides
    public Context provideAppContext() {
        return mApp.getApplicationContext();
    }
}
