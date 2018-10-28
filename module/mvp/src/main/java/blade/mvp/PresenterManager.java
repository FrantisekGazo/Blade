package blade.mvp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
public final class PresenterManager {

    //region public API

    /**
     * Unbinds and removes all presenters for given <code>view</code>.
     *
     * @param view MVP view
     */
    public static void removeAllFor(@NonNull final IView view) {
        getInstance().removePresenters(view);
    }

    //endregion public API

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
    public final String getId(@NonNull final WeavedMvpActivity view) {
        nonNull(view, "view");

        String id = null;

        final Bundle viewState = view.getWeavedState();
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
    public final String getId(@NonNull final WeavedMvpUi view, @NonNull final Context activityContext) {
        nonNull(view, "view");
        nonNull(view, "activityContext");

        if (view instanceof View) {
            final View v = (View) view;
            if (v.getId() == View.NO_ID) {
                throw new IllegalStateException("View does not have an ID. Without it the state of the view won't be managed.");
            }
        }

        if (activityContext instanceof WeavedMvpActivity) {
            String id = null;

            final Bundle viewState = view.getWeavedState();
            if (viewState != null) {
                id = viewState.getString(STATE_VIEW_ID);
            }

            if (id == null) {
                final WeavedMvpActivity mvpActivity = (WeavedMvpActivity) activityContext;

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
    public final void saveViewId(@NonNull final Bundle state, @NonNull final String id) {
        state.putString(STATE_VIEW_ID, id);
    }

    @NonNull
    private String getActivityIdPart(@NonNull final WeavedMvpUi view) {
        final String fragmentId = view.getWeavedId();
        final String[] ids = fragmentId.split(ID_SEPARATOR);
        return ids[0];
    }

    //endregion ID management

    //region provide/create/bind presenter

    /**
     * Checks if a presenter exists for given view.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    public final <V extends IView & WeavedMvpUi>
    boolean exists(@NonNull final V view, @NonNull final String fieldName) {
        nonNull(view, "view");
        nonNull(fieldName, "filedName");

        final ActivityPresenterManager apm;

        if (view instanceof WeavedMvpActivity) {
            final WeavedMvpActivity mvpActivity = (WeavedMvpActivity) view;

            final String activityId = mvpActivity.getWeavedId();
            apm = getActivityPresenterManagerOrCreate(activityId);
        } else if (view instanceof WeavedMvpFragment) {
            final WeavedMvpFragment mvpFragment = (WeavedMvpFragment) view;

            final String activityId = getActivityIdPart(mvpFragment);
            apm = getActivityPresenterManagerOrCreate(activityId);
        } else if (view instanceof WeavedMvpView) {
            final WeavedMvpView mvpView = (WeavedMvpView) view;

            final String activityId = getActivityIdPart(mvpView);
            apm = getActivityPresenterManagerOrCreate(activityId);
        } else {
            throw new IllegalArgumentException("view has unsupported type");
        }

        return apm.get(view, fieldName) != null;
    }

    /**
     * Provides presenter.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    @NonNull
    public final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
    P getInitialized(@NonNull final V view, @NonNull final String fieldName) {
        nonNull(view, "view");
        nonNull(fieldName, "filedName");

        final ActivityPresenterManager apm = findActivityPresenterManager(view);

        P presenter = apm.get(view, fieldName);
        if (presenter == null) {
            throw new IllegalStateException("presenter is missing");
        }
        presenter.onBind(view);

        return presenter;
    }

    /**
     * Provides presenter.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    @NonNull
    public final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
    P initialize(@NonNull final V view, @NonNull final String fieldName, @NonNull final P presenter) {
        nonNull(view, "view");
        nonNull(fieldName, "filedName");
        nonNull(presenter, "presenter");

        final ActivityPresenterManager apm = findActivityPresenterManager(view);

        apm.put(view, fieldName, presenter);

        final Bundle viewState = view.getWeavedState();
        final Bundle presenterState = (viewState != null) ? viewState.getBundle(getPresenterStateKey(fieldName)) : null;
        presenter.onCreate(presenterState);

        presenter.onBind(view);

        return presenter;
    }

    /**
     * Provides presenter.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    @NonNull
    public final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
    P get(@NonNull final V view, @NonNull final String fieldName, @NonNull final Provider provider) {
        nonNull(view, "view");
        nonNull(fieldName, "filedName");
        nonNull(provider, "provider");

        final ActivityPresenterManager apm = findActivityPresenterManager(view);

        P presenter = apm.get(view, fieldName);
        if (presenter == null) {
            // cast it here instead of requiring Provider<P> as parameter because of the weaved code
            //noinspection unchecked
            presenter = (P) provider.get();
            apm.put(view, fieldName, presenter);

            final Bundle viewState = view.getWeavedState();
            final Bundle presenterState = (viewState != null) ? viewState.getBundle(getPresenterStateKey(fieldName)) : null;
            presenter.onCreate(presenterState);
        }
        presenter.onBind(view);

        return presenter;
    }

    private ActivityPresenterManager findActivityPresenterManager(@NonNull final IView view) {
        if (view instanceof WeavedMvpActivity) {
            final WeavedMvpActivity mvpActivity = (WeavedMvpActivity) view;

            final String activityId = mvpActivity.getWeavedId();
            return getActivityPresenterManagerOrCreate(activityId);
        } else if (view instanceof WeavedMvpFragment) {
            final WeavedMvpFragment mvpFragment = (WeavedMvpFragment) view;

            final String activityId = getActivityIdPart(mvpFragment);
            return getActivityPresenterManagerOrCreate(activityId);
        } else if (view instanceof WeavedMvpView) {
            final WeavedMvpView mvpView = (WeavedMvpView) view;

            final String activityId = getActivityIdPart(mvpView);
            return getActivityPresenterManagerOrCreate(activityId);
        } else {
            throw new IllegalArgumentException("view has unsupported type");
        }
    }

    //endregion provide presenter

    //region save presenter

    /**
     * Saves given presenter.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    public final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
    void save(@NonNull final Bundle outState, @NonNull final String fieldName, @Nullable final P presenter) {
        if (presenter == null) {
            return;
        }

        nonNull(fieldName, "fieldName");
        nonNull(outState, "outState");

        final Bundle presenterState = new Bundle();
        presenter.onSaveState(presenterState);
        outState.putBundle(getPresenterStateKey(fieldName), presenterState);
    }

    @NonNull
    private String getPresenterStateKey(@NonNull final String fieldName) {
        return String.format(STATE_VIEW_PRESENTER, fieldName);
    }

    //endregion save presenter

    //region unbind/remove presenter

    /**
     * Performs some actions when activity is being destroyed.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    public final <V extends Activity & IView & WeavedMvpUi, P extends IPresenter<V>>
    void onActivityDestroy(@NonNull final V view, @NonNull final String fieldName, @Nullable final P presenter) {
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
    void onFragmentDestroy(@NonNull final V view, @NonNull final String fieldName, @Nullable final P presenter) {
        if (presenter == null) {
            return;
        }

        nonNull(view, "view");
        nonNull(fieldName, "fieldName");

        if (presenter.getView() != null) {
            presenter.onUnbind();
        }

        boolean remove = false;
        final Activity activity = view.getActivity();
        if (activity != null && activity.isFinishing()) {
            // if activity is finishing the fragment will too
            remove = true;
        } else if (view.isRemoving() && !view.wasOnSaveCalled()) {
            // The fragment can be still in backstack even if isRemoving() is true.
            // We check waOnSaveCalled() - if this was not called then the fragment is totally removed.
            remove = true;
        }

        if (remove) {
            final String activityId = getActivityIdPart(view);
            final ActivityPresenterManager apm = getActivityPresenterManager(activityId);
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
    void onFragmentDestroyView(@NonNull final V view, @NonNull final String fieldName, @Nullable final P presenter) {
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
            final String activityId = getActivityIdPart(view);
            final ActivityPresenterManager apm = getActivityPresenterManager(activityId);
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
    void onViewDestroy(@NonNull final V view, @NonNull final String fieldName, @Nullable final P presenter) {
        if (presenter == null) {
            return;
        }

        nonNull(view, "view");
        nonNull(fieldName, "fieldName");

        if (presenter.getView() != null) {
            presenter.onUnbind();
        }

        boolean remove = false;
        final Activity activity = (Activity) view.getContext();
        if (activity != null && activity.isFinishing()) {
            // if activity is finishing the fragment will too
            remove = true;
        } else if (!view.wasOnSaveCalled()) {
            // We check waOnSaveCalled() - if this was not called then the view will be removed.
            remove = true;
        }

        if (remove) {
            final String activityId = getActivityIdPart(view);
            final ActivityPresenterManager apm = getActivityPresenterManager(activityId);
            if (apm != null) {
                apm.remove(view, fieldName);
            }
        }
    }

    //endregion unbind/remove presenter

    //region remove manually

    /**
     * Unbinds and removes all presenters for given <code>view</code>.
     * <p/>
     * NOTE: This method is only for internal usage of this library. Do not call it manually.
     */
    private void removePresenters(@NonNull final IView view) {
        if (view instanceof WeavedMvpActivity) {
            final WeavedMvpActivity mvpActivity = (WeavedMvpActivity) view;
            removeActivityPresenterManager(mvpActivity.getWeavedId());
        } else if (view instanceof WeavedMvpFragment) {
            final WeavedMvpFragment mvpFragment = (WeavedMvpFragment) view;
            final ActivityPresenterManager apm = getActivityPresenterManager(mvpFragment.getWeavedId());
            if (apm != null) {
                apm.removeFor(mvpFragment, false);
            }
        } else if (view instanceof WeavedMvpView) {
            final WeavedMvpView mvpView = (WeavedMvpView) view;
            final ActivityPresenterManager apm = getActivityPresenterManager(mvpView.getWeavedId());
            if (apm != null) {
                apm.removeFor(mvpView, false);
            }
        } else {
            throw new IllegalArgumentException("Unsupported view class");
        }
    }

    //endregion remove manually

    //region ActivityPresenterManager management

    @Nullable
    private ActivityPresenterManager getActivityPresenterManager(@NonNull final String activityId) {
        return mActivityManagers.get(activityId);
    }

    @NonNull
    private ActivityPresenterManager getActivityPresenterManagerOrCreate(@NonNull final String activityId) {
        if (mActivityManagers.containsKey(activityId)) {
            return mActivityManagers.get(activityId);
        } else {
            final ActivityPresenterManager apm = new ActivityPresenterManager();
            mActivityManagers.put(activityId, apm);
            return apm;
        }
    }

    private void removeActivityPresenterManager(@NonNull final String activityId) {
        if (mActivityManagers.containsKey(activityId)) {
            final ActivityPresenterManager apm = mActivityManagers.get(activityId);
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
        void put(@NonNull final V view, @NonNull final String filedName, @NonNull final P presenter) {
            final String key = prepareKey(view, filedName);
            mPresenters.put(key, presenter);
        }

        @Nullable
        final <V extends IView & WeavedMvpUi, P extends IPresenter<V>>
        P get(@NonNull final V view, @NonNull final String filedName) {
            final String key = prepareKey(view, filedName);
            //noinspection unchecked
            return (P) mPresenters.get(key);
        }

        /**
         * Calls {@link IPresenter#onDestroy()} and forgets reference for presenter in given view.
         */
        final void remove(@NonNull final WeavedMvpUi view, @NonNull final String fieldName) {
            final String key = prepareKey(view, fieldName);

            final IPresenter presenter = mPresenters.remove(key);
            if (presenter != null) {
                presenter.onDestroy();
            }
        }

        /**
         * Calls {@link IPresenter#onDestroy()} and forgets reference for all presenters in given view.
         */
        final void removeFor(@NonNull final WeavedMvpUi view, final boolean skipUnbind) {
            final String keyPrefix = prepareKey(view, "");

            final Set<String> keys = mPresenters.keySet();
            for (final String key : keys) {
                if (key.startsWith(keyPrefix)) {
                    final IPresenter presenter = mPresenters.remove(key);
                    if (presenter != null) {
                        if (!skipUnbind && presenter.getView() != null) {
                            presenter.onUnbind();
                        }
                        presenter.onDestroy();
                    }
                }
            }
        }

        /**
         * Calls {@link IPresenter#onDestroy()} and forgets reference for all presenters.
         */
        final void removeAll() {
            final Collection<IPresenter> presenters = mPresenters.values();
            for (final IPresenter presenter : presenters) {
                presenter.onDestroy();
            }
            mPresenters.clear();
        }

        @NonNull
        private String prepareKey(@NonNull final WeavedMvpUi view, @NonNull final String filedName) {
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
