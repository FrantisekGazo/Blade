package eu.f3rog.blade.sample.mvp.ui;

import android.app.Application;

import eu.f3rog.blade.sample.mvp.di.component.Component;


public final class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Component.initAppComponent(this);
    }
}
