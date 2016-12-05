package blade.mvp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import javax.inject.Provider;

import eu.f3rog.blade.mvp.WeavedMvpActivity;
import eu.f3rog.blade.mvp.WeavedMvpFragment;
import eu.f3rog.blade.mvp.WeavedMvpUi;
import eu.f3rog.blade.mvp.WeavedMvpView;

/**
 * Class {@link PresenterManager}
 *
 * @author FrantisekGazo
 */
public class PresenterManager {

    private static final PresenterManager sInstance = new PresenterManager();
    private static final String STATE_VIEW_ID = "blade:view-id";
    private static final String STATE_VIEW_PRESENTER = "blade:view-%s";
    private static final String ID_SEPARATOR = ">-<";

    public static PresenterManager getInstance() {
        return sInstance;
    }

    private final Map<String, ActivityPresenterManager> mActivityManagers;

    private PresenterManager() {
        mActivityManagers = new HashMap<>();
    }

    //region ID management

    /**
     * Returns ID for given view.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     *
     * @param view Activity instance
     * @return ID
     */
    @NonNull
    public final String getId(@NonNull WeavedMvpActivity view) {
        nonNull(view, "view");

        String id = null;

        Bundle viewState = view.getWeavedState();
        if (viewState != null) {
            id = viewState.getString(STATE_VIEW_ID);
        }

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        return id;
    }

    /**
     * Returns ID for given view.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     *
     * @param view            View or Fragment instance
     * @param activityContext Activity context
     * @return ID
     */
    @NonNull
    public final String getId(@NonNull WeavedMvpUi view, @NonNull Context activityContext) {
        nonNull(view, "view");
        nonNull(view, "activityContext");

        if (view instanceof View) {
            View v = (View) view;
            if (v.getId() == View.NO_ID) {
                throw new IllegalStateException("View does not have an ID. Without it the state of the view won't be managed.");
            }
        }

        if (activityContext instanceof WeavedMvpActivity) {
            String id = null;

            Bundle viewState = view.getWeavedState();
            if (viewState != null) {
                id = viewState.getString(STATE_VIEW_ID);
            }

            if (id == null) {
                WeavedMvpActivity mvpActivity = (WeavedMvpActivity) activityContext;

                id = mvpActivity.getWeavedId() + ID_SEPARATOR + UUID.randomUUID().toString();
            }

            return id;
        } else {
            throw new IllegalStateException(String.format(
                    "View %s is in activity that does not support MVP. Annotate %s with @Blade to solve the problem.",
                    view, activityContext.getClass().getCanonicalName())
            );
        }
    }

    /**
     * Saves given ID into given state
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     *
     * @param state MVP view state
     * @param id    MVP view ID
     */
    public final void saveViewId(@NonNull Bundle state, @NonNull String id) {
        state.putString(STATE_VIEW_ID, id);
    }

    @NonNull
    private String getActivityIdPart(@NonNull WeavedMvpUi view) {
        String fragmentId = view.getWeavedId();
        String[] ids = fragmentId.split(ID_SEPARATOR);
        return ids[0];
    }

    //endregion ID management

    //region provide/create/bind presenter

    /**
     * Provides presenter.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    @NonNull
    public final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
    P get(@NonNull V view, @NonNull String fieldName, @NonNull Provider<P> provider) {
        nonNull(view, "view");
        nonNull(fieldName, "filedName");
        nonNull(provider, "provider");

        ActivityPresenterManager apm;

        if (view instanceof WeavedMvpActivity) {
            WeavedMvpActivity mvpActivity = (WeavedMvpActivity) view;

            String activityId = mvpActivity.getWeavedId();
            apm = getActivityPresenterManagerOrCreate(activityId);
        } else if (view instanceof WeavedMvpFragment) {
            WeavedMvpFragment mvpFragment = (WeavedMvpFragment) view;

            String activityId = getActivityIdPart(mvpFragment);
            apm = getActivityPresenterManagerOrCreate(activityId);
        } else if (view instanceof WeavedMvpView) {
            WeavedMvpView mvpView = (WeavedMvpView) view;

            String activityId = getActivityIdPart(mvpView);
            apm = getActivityPresenterManagerOrCreate(activityId);
        } else {
            throw new IllegalArgumentException("view has unsupported type");
        }

        P presenter = apm.get(view, fieldName);
        if (presenter == null) {
            presenter = provider.get();
            apm.put(view, fieldName, presenter);

            presenter.onCreate(view.getWeavedState());
        }
        presenter.onBind(view);

        return presenter;
    }

    //endregion provide presenter

    //region save presenter

    /**
     * Saves given presenter.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    public final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
    void save(@NonNull Bundle outState, @NonNull String fieldName, @Nullable P presenter) {
        if (presenter == null) {
            return;
        }

        nonNull(fieldName, "fieldName");
        nonNull(outState, "outState");

        Bundle presenterState = new Bundle();
        presenter.onSaveState(presenterState);
        outState.putBundle(String.format(STATE_VIEW_PRESENTER, fieldName), presenterState);
    }

    //endregion save presenter

    //region unbind/remove presenter

    /**
     * Performs some actions when activity is being destroyed.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    public final <V extends Activity & IView & WeavedMvpUi, P extends IPresenter<V>>
    void onActivityDestroy(@NonNull V view, @NonNull String fieldName, @Nullable P presenter) {
        if (presenter == null) {
            return;
        }

        nonNull(view, "view");
        nonNull(fieldName, "fieldName");

        if (presenter.getView() != null) {
            presenter.onUnbind();
        }

        // check if presenter should be removed completely
        if (view.isFinishing()) {
            removeActivityPresenterManager(view.getWeavedId());
        }
    }

    /**
     * Performs some actions when fragment is being destroyed.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    public final <V extends Fragment & IView & WeavedMvpFragment, P extends IPresenter<V>>
    void onFragmentDestroy(@NonNull V view, @NonNull String fieldName, @Nullable P presenter) {
        if (presenter == null) {
            return;
        }

        nonNull(view, "view");
        nonNull(fieldName, "fieldName");

        if (presenter.getView() != null) {
            presenter.onUnbind();
        }

        boolean remove = false;
        Activity activity = view.getActivity();
        if (activity != null && activity.isFinishing()) {
            // if activity is finishing the fragment will too
            remove = true;
        } else if (view.isRemoving() && !view.wasOnSaveCalled()) {
            // The fragment can be still in backstack even if isRemoving() is true.
            // We check waOnSaveCalled() - if this was not called then the fragment is totally removed.
            remove = true;
        }

        if (remove) {
            String activityId = getActivityIdPart(view);
            ActivityPresenterManager apm = getActivityPresenterManager(activityId);
            if (apm != null) {
                apm.remove(view, fieldName);
            }
        }
    }

    /**
     * Performs some actions when fragment's view is being destroyed.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    public final <V extends Fragment & IView & WeavedMvpFragment, P extends IPresenter<V>>
    void onFragmentDestroyView(@NonNull V view, @NonNull String fieldName, @Nullable P presenter) {
        if (presenter == null) {
            return;
        }

        nonNull(view, "view");
        nonNull(fieldName, "fieldName");

        if (presenter.getView() != null) {
            presenter.onUnbind();
        }

        // check if presenter should be removed completely
        if (view.getActivity() != null && view.getActivity().isFinishing()) {
            // if activity is finishing the fragment will too
            String activityId = getActivityIdPart(view);
            ActivityPresenterManager apm = getActivityPresenterManager(activityId);
            if (apm != null) {
                apm.remove(view, fieldName);
            }
        }
    }

    /**
     * Performs some actions when view is being destroyed.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    public final <V extends View & IView & WeavedMvpView, P extends IPresenter<V>>
    void onViewDestroy(@NonNull V view, @NonNull String fieldName, @Nullable P presenter) {
        if (presenter == null) {
            return;
        }

        nonNull(view, "view");
        nonNull(fieldName, "fieldName");

        if (presenter.getView() != null) {
            presenter.onUnbind();
        }

        boolean remove = false;
        Activity activity = (Activity) view.getContext();
        if (activity != null && activity.isFinishing()) {
            // if activity is finishing the fragment will too
            remove = true;
        } else if (!view.wasOnSaveCalled()) {
            // We check waOnSaveCalled() - if this was not called then the view will be removed.
            remove = true;
        }

        if (remove) {
            String activityId = getActivityIdPart(view);
            ActivityPresenterManager apm = getActivityPresenterManager(activityId);
            if (apm != null) {
                apm.remove(view, fieldName);
            }
        }
    }

    //endregion unbind/remove presenter

    //region ActivityPresenterManager management

    @Nullable
    private ActivityPresenterManager getActivityPresenterManager(@NonNull String activityId) {
        return mActivityManagers.get(activityId);
    }

    @NonNull
    private ActivityPresenterManager getActivityPresenterManagerOrCreate(@NonNull String activityId) {
        if (mActivityManagers.containsKey(activityId)) {
            return mActivityManagers.get(activityId);
        } else {
            ActivityPresenterManager apm = new ActivityPresenterManager();
            mActivityManagers.put(activityId, apm);
            return apm;
        }
    }

    private void removeActivityPresenterManager(@NonNull String activityId) {
        if (mActivityManagers.containsKey(activityId)) {
            ActivityPresenterManager apm = mActivityManagers.get(activityId);
            apm.removeAll();
            mActivityManagers.remove(activityId);
        }
    }

    //endregion ActivityPresenterManager management

    private static final class ActivityPresenterManager {

        private final Map<String, IPresenter> mPresenters;

        ActivityPresenterManager() {
            mPresenters = new HashMap<>();
        }

        final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
        void put(@NonNull V view, @NonNull String filedName, @NonNull P presenter) {
            String key = prepareKey(view, filedName);
            mPresenters.put(key, presenter);
        }

        @Nullable
        final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
        P get(@NonNull V view, @NonNull String filedName) {
            String key = prepareKey(view, filedName);
            //noinspection unchecked
            return (P) mPresenters.get(key);
        }

        /**
         * Calls {@link IPresenter#onDestroy()} and forgets reference for presenter in given view.
         */
        final void remove(@NonNull WeavedMvpUi view, @NonNull String fieldName) {
            String key = prepareKey(view, fieldName);

            IPresenter presenter = mPresenters.remove(key);
            if (presenter != null) {
                presenter.onDestroy();
            }
        }

        /**
         * Calls {@link IPresenter#onDestroy()} and forgets reference for all presenters.
         */
        final void removeAll() {
            Collection<IPresenter> presenters = mPresenters.values();
            for (IPresenter presenter : presenters) {
                presenter.onDestroy();
            }
            mPresenters.clear();
        }

        @NonNull
        private String prepareKey(@NonNull WeavedMvpUi view, @NonNull String filedName) {
            return view.getWeavedId() + ID_SEPARATOR + filedName;
        }
    }

    //region util methods

    private void nonNull(Object o, String name) {
        if (o == null) {
            throw new IllegalArgumentException(String.format("'%s' cannot be null", name));
        }
    }

    //endregion util methods
}
