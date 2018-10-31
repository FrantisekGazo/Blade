package eu.f3rog.blade.sample.mvp.di.component;

import android.app.Application;
import androidx.annotation.NonNull;

import eu.f3rog.blade.sample.mvp.di.module.app.AppModule;
import eu.f3rog.blade.sample.mvp.di.module.data.DataModule;
import eu.f3rog.blade.sample.mvp.di.module.presenter.PresenterModule;

/**
 * Class {@link Component}
 *
 * @author FrantisekGazo
 */
public class Component {

    private static AppComponent sAppComponent = null;

    public static AppComponent forApp() {
        return sAppComponent;
    }

    public static void initAppComponent(Application app) {
        sAppComponent = createAppComponent(new AppModule(app));
    }

    public static AppComponent createAppComponent(@NonNull final AppModule appModule) {
        return DaggerAppComponent
                .builder()
                .appModule(appModule)
                .dataModule(new DataModule())
                .presenterModule(new PresenterModule())
                .build();
    }
}
