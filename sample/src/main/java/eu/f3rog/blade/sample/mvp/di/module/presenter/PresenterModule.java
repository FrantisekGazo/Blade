package eu.f3rog.blade.sample.mvp.di.module.presenter;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import eu.f3rog.blade.sample.mvp.service.DataService;
import eu.f3rog.blade.sample.mvp.presenter.ActorListPresenter;
import eu.f3rog.blade.sample.mvp.presenter.ActorPresenter;
import rx.Scheduler;


@Module
public class PresenterModule {

    @Provides
    public ActorListPresenter provideActorListPresenter(@NonNull final DataService dataService) {
        return new ActorListPresenterImpl(dataService);
    }

    @Provides
    public ActorPresenter provideActorPresenter(@NonNull final DataService dataService) {
        return new ActorPresenterImpl(dataService);
    }
}
