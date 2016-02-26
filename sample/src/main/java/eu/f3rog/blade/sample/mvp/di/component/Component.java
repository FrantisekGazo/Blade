package eu.f3rog.blade.sample.mvp.di.component;

import eu.f3rog.blade.sample.mvp.di.module.RxModule;

/**
 * Class {@link Component}
 *
 * @author FrantisekGazo
 * @version 2016-02-26
 */
public class Component {

    private static AppComponent sAppComponent = null;

    public static AppComponent forApp() {
        if (sAppComponent == null) {
            sAppComponent = DaggerAppComponent
                    .builder()
                    .rxModule(new RxModule())
                    .build();
        }
        return sAppComponent;
    }

    public static void setForApp(AppComponent component) {
        sAppComponent = component;
    }

}
