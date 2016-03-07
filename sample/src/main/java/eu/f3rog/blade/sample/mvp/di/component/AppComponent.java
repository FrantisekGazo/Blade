package eu.f3rog.blade.sample.mvp.di.component;

import dagger.Component;
import eu.f3rog.blade.sample.mvp.di.module.RxModule;
import eu.f3rog.blade.sample.mvp.presenter.DataPresenter;

@Component(
        modules = {
                RxModule.class
        }
)
public interface AppComponent {

    void inject(DataPresenter dataPresenter);
}
