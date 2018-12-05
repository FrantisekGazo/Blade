package eu.f3rog.blade.sample.mvp.di.module.data;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.f3rog.blade.sample.mvp.di.module.data.api.ActorApi;
import eu.f3rog.blade.sample.mvp.di.module.data.api.JsonActor;
import eu.f3rog.blade.sample.mvp.di.module.data.api.JsonActorInfo;
import eu.f3rog.blade.sample.mvp.model.Actor;
import eu.f3rog.blade.sample.mvp.model.ActorDetail;
import eu.f3rog.blade.sample.mvp.service.DataService;
import rx.Single;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/* package */ class OnlineDataService implements DataService {

    @NonNull
    private final ActorApi mActorApi;

    public OnlineDataService(@NonNull final ActorApi actorApi) {
        mActorApi = actorApi;
    }

    @NonNull
    @Override
    public Single<List<Actor>> getAllActors() {
        return mActorApi.getAllActors()
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<JsonActor>, List<Actor>>() {
                    @Override
                    public List<Actor> call(@NonNull final List<JsonActor> jsonActors) {
                        final List<Actor> actors = new ArrayList<>();
                        for (final JsonActor a : jsonActors) {
                            actors.add(new Actor(a.id, a.name));
                        }
                        return actors;
                    }
                });
    }

    @NonNull
    @Override
    public Single<ActorDetail> getActorDetail(final long id) {
        return mActorApi.getActorInfo(id)
                .subscribeOn(Schedulers.io())
                .map(new Func1<JsonActorInfo, ActorDetail>() {
                    @Override
                    public ActorDetail call(@NonNull final JsonActorInfo a) {
                        return new ActorDetail(a.birthName, a.birthDate, a.bio);
                    }
                });
    }
}
