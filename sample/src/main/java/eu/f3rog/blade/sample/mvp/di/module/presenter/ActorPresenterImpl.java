package eu.f3rog.blade.sample.mvp.di.module.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import blade.State;
import eu.f3rog.blade.sample.mvp.model.ActorDetail;
import eu.f3rog.blade.sample.mvp.presenter.ActorPresenter;
import eu.f3rog.blade.sample.mvp.service.DataService;
import eu.f3rog.blade.sample.mvp.view.ActorView;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/* package */ final class ActorPresenterImpl extends ActorPresenter {

    @NonNull
    private final DataService mDataService;

    @State
    long mActorId;

    @Nullable
    private Subscription mSubscription;
    @Nullable
    private ActorDetail mLoadedActorDetail;
    @Nullable
    private String mErrorMessage;

    public ActorPresenterImpl(@NonNull final DataService dataService) {
        this.mDataService = dataService;
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
        mLoadedActorDetail = null;
        mErrorMessage = null;
        mSubscription = mDataService.getActorDetail(mActorId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ActorDetail>() {
                    @Override
                    public void onSuccess(@NonNull final ActorDetail actorDetail) {
                        mLoadedActorDetail = actorDetail;
                        showIn(getView());
                    }

                    @Override
                    public void onError(Throwable error) {
                        mErrorMessage = error.getMessage();
                        showIn(getView());
                    }
                });
    }

    private void showIn(@Nullable final ActorView view) {
        if (view == null) {
            return;
        }

        if (mErrorMessage != null) {
            view.showError(mErrorMessage);
        } else if (mLoadedActorDetail != null) {
            view.show(mLoadedActorDetail);
        } else {
            view.showProgress();
        }
    }

    @Override
    public void onBind(@NonNull ActorView view) {
        super.onBind(view);
        showIn(view);
    }

    @Override
    public void setActorId(final long id) {
        if (mActorId != id) {
            mActorId = id;
            startLoading();
        }
    }
}
