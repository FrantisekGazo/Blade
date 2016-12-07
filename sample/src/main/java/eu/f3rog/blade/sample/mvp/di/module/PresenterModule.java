package eu.f3rog.blade.sample.mvp.di.module;

import dagger.Module;
import dagger.Provides;
import eu.f3rog.blade.sample.mvp.di.q.SchedulerType;
import eu.f3rog.blade.sample.mvp.presenter.DataPresenter;
import rx.Scheduler;


@Module
public class PresenterModule {

    @Provides
    public DataPresenter providesDataPresenter(@SchedulerType.Main Scheduler s1, @SchedulerType.Task Scheduler s2) {
        return new DataPresenter(s1, s2);
    }
}
