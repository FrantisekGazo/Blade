package eu.f3rog.blade.sample.mvp.presenter;


import androidx.annotation.NonNull;

import blade.mvp.BasePresenter;
import eu.f3rog.blade.sample.mvp.model.Actor;
import eu.f3rog.blade.sample.mvp.view.ActorListView;

public abstract class ActorListPresenter extends BasePresenter<ActorListView> {

    public abstract void onActorSelected(@NonNull final Actor actor);
}
