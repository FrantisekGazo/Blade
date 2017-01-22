package eu.f3rog.blade.sample.mvp.service;

import android.support.annotation.NonNull;

import java.util.List;

import eu.f3rog.blade.sample.mvp.model.Actor;
import eu.f3rog.blade.sample.mvp.model.ActorDetail;
import rx.Single;


public interface DataService {

    @NonNull
    Single<List<Actor>> getAllActors();

    @NonNull
    Single<ActorDetail> getActorDetail(final long id);
}
