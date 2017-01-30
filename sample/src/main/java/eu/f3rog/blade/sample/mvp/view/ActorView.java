package eu.f3rog.blade.sample.mvp.view;


import android.support.annotation.NonNull;

import blade.mvp.IView;
import eu.f3rog.blade.sample.mvp.model.ActorDetail;

public interface ActorView extends IView {

    void showProgress();

    void showError(@NonNull final String errorMessage);

    void show(@NonNull final ActorDetail actorDetail);
}
