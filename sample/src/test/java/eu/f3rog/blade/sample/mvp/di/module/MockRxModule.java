package eu.f3rog.blade.sample.mvp.di.module;

import rx.Scheduler;
import rx.schedulers.Schedulers;

public class MockRxModule extends RxModule {

    public Scheduler providesMainScheduler() {
        return Schedulers.immediate();
    }

    public Scheduler providesTaskScheduler() {
        return Schedulers.immediate();
    }

}
