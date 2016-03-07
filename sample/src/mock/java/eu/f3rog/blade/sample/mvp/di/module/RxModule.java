package eu.f3rog.blade.sample.mvp.di.module;

import dagger.Module;
import dagger.Provides;
import eu.f3rog.blade.sample.mvp.di.q.SchedulerType;
import rx.Scheduler;
import rx.schedulers.Schedulers;

@Module
public class RxModule {

    @Provides
    @SchedulerType.Main
    public Scheduler providesMainScheduler() {
        return Schedulers.immediate();
    }

    @Provides
    @SchedulerType.Task
    public Scheduler providesTaskScheduler() {
        return Schedulers.immediate();
    }

}