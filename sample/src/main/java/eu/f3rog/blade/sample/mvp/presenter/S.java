package eu.f3rog.blade.sample.mvp.presenter;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * rx
 * Class {@link S}
 *
 * @author FrantisekGazo
 * @version 2016-02-19
 */
public class S {

    public static Scheduler getMainScheduler() {
        return AndroidSchedulers.mainThread();
    }

    public static Scheduler getBackgroundScheduler() {
        return Schedulers.newThread();
    }

}
