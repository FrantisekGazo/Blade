package eu.f3rog.blade.sample.mvp.di.module.data.api;


import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Single;

/**
 * A mock api that is created via apiari.io
 */
public interface ActorApi {

    String ENDPOINT = "http://private-d2a21-blade2.apiary-mock.com/";

    @GET("actors")
    Single<List<JsonActor>> getAllActors();

    @GET("actor/{id}")
    Single<JsonActorInfo> getActorInfo(@Path("id") final long id);
}
