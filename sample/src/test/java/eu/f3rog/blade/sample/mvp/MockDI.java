package eu.f3rog.blade.sample.mvp;

import eu.f3rog.blade.sample.mvp.service.SimpleDI;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class MockDI implements SimpleDI {

    public Scheduler getMainScheduler() {
        return Schedulers.immediate();
    }

    public Scheduler getBackgroundScheduler() {
        return Schedulers.immediate();
    }

}
