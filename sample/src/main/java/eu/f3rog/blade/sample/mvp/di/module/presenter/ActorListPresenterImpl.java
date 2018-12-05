package eu.f3rog.blade.sample.mvp.di.module.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import eu.f3rog.blade.sample.mvp.model.Actor;
import eu.f3rog.blade.sample.mvp.presenter.ActorListPresenter;
import eu.f3rog.blade.sample.mvp.service.DataService;
import eu.f3rog.blade.sample.mvp.view.ActorListView;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/* package */ final class ActorListPresenterImpl extends ActorListPresenter {

    @NonNull
    private final DataService mDataService;

    @Nullable
    private Subscription mSubscription;
    @Nullable
    private List<Actor> mLoadedActors;
    @Nullable
    private String mErrorMessage;

    public ActorListPresenterImpl(@NonNull final DataService dataService) {
        this.mDataService = dataService;
    }

    @Override
    public void onCreate(@Nullable Object state) {
        super.onCreate(state);

        if (mLoadedActors == null) {
            startLoading();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    private void startLoading() {
        mLoadedActors = null;
        mErrorMessage = null;
        mSubscription = mDataService.getAllActors()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<List<Actor>>() {
                    @Override
                    public void onSuccess(@NonNull final List<Actor> actors) {
                        mLoadedActors = actors;
                        showIn(getView());
                    }

                    @Override
                    public void onError(Throwable error) {
                        mErrorMessage = error.getMessage();
                        showIn(getView());
                    }
                });
    }

    private void showIn(@Nullable final ActorListView view) {
        if (view == null) {
            return;
        }

        if (mErrorMessage != null) {
            view.showError(mErrorMessage);
        } else if (mLoadedActors != null) {
            view.show(mLoadedActors);
        } else {
            view.showProgress();
        }
    }

    @Override
    public void onBind(@NonNull ActorListView view) {
        super.onBind(view);
        showIn(view);
    }

    @Override
    public void onActorSelected(@NonNull final Actor actor) {
        if (getView() != null) {
            getView().gotoActorDetail(actor);
        }
    }
}
