package eu.f3rog.blade.sample.mvp.di.component;

import dagger.Component;
import eu.f3rog.blade.sample.mvp.ui.activity.TestMvpActivity;
import eu.f3rog.blade.sample.mvp.di.module.PresenterModule;
import eu.f3rog.blade.sample.mvp.di.module.RxModule;
import eu.f3rog.blade.sample.mvp.ui.fragment.TestMvpFragment;

@Component(modules = {
        RxModule.class,
        PresenterModule.class
})
public interface AppComponent {

    void inject(TestMvpActivity activity);

    void inject(TestMvpFragment fragment);
}
