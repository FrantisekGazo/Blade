package eu.f3rog.blade.sample.mvp.service;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * rx
 * Class {@link AppDI}
 *
 * @author FrantisekGazo
 * @version 2016-02-19
 */
public class AppDI implements SimpleDI {

    public static SimpleDI sInstance = null;

    public static SimpleDI getInstance() {
        if (sInstance == null) {
            sInstance = new AppDI();
        }
        return sInstance;
    }

    public Scheduler getMainScheduler() {
        return AndroidSchedulers.mainThread();
    }

    public Scheduler getBackgroundScheduler() {
        return Schedulers.newThread();
    }
}
