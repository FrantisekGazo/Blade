package eu.f3rog.blade.sample.mvp.di.component;

import javax.inject.Singleton;

import dagger.Component;
import eu.f3rog.blade.sample.mvp.di.module.app.AppModule;
import eu.f3rog.blade.sample.mvp.di.module.data.DataModule;
import eu.f3rog.blade.sample.mvp.di.module.presenter.PresenterModule;
import eu.f3rog.blade.sample.mvp.presenter.ActorListPresenter;
import eu.f3rog.blade.sample.mvp.ui.activity.ActorsActivity;
import eu.f3rog.blade.sample.mvp.ui.fragment.ActorDialogFragment;
import eu.f3rog.blade.sample.mvp.ui.fragment.ActorFragment;
import eu.f3rog.blade.sample.mvp.ui.view.ActorCustomView;

@Singleton
@Component(modules = {
        AppModule.class,
        DataModule.class,
        PresenterModule.class
})
public interface AppComponent {

    void inject(ActorsActivity activity);

    void inject(ActorFragment fragment);

    void inject(ActorDialogFragment fragment);

    void inject(ActorCustomView view);

    // for tests
    ActorListPresenter actorListPresenter();
}
