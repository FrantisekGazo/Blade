package eu.f3rog.blade.sample.mvp.view;


import androidx.annotation.NonNull;

import java.util.List;

import blade.mvp.IView;
import eu.f3rog.blade.sample.mvp.model.Actor;

public interface ActorListView extends IView {

    void showProgress();

    void showError(@NonNull final String errorMessage);

    void show(@NonNull final List<Actor> actors);

    void gotoActorDetail(@NonNull final  Actor actor);
}
