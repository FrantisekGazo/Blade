package eu.f3rog.blade.sample.mvp.presenter;


import blade.mvp.BasePresenter;
import eu.f3rog.blade.sample.mvp.view.ActorView;

public abstract class ActorPresenter extends BasePresenter<ActorView> {

    public abstract void setActorId(final long id);
}
