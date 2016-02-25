package blade.mvp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.f3rog.blade.mvp.MvpActivity;

/**
 * Class {@link PresenterManager}
 *
 * @author FrantisekGazo
 * @version 2016-02-12
 */
public class PresenterManager {

    /**
     * Used internally by Blade library.
     */
    public static <V extends IView, D> void put(V view, D data, IPresenter<V, D> presenter) {
        assert view != null;
        assert data != null;
        assert presenter != null;

        if (view instanceof View) {
            View v = (View) view;
            forParentActivity(v).put(v, data, presenter);
        } else if (view instanceof Activity) {
            Activity a = (Activity) view;
            forActivity(a).put(data, presenter);
        } else {
            throw new IllegalArgumentException("View has to be instance of android View or Activity.");
        }
    }

    /**
     * Used internally by Blade library.
     */
    public static <V extends IView, D> IPresenter get(V view, D data, Class presenterClass) {
        assert view != null;
        assert data != null;
        assert presenterClass != null;

        if (view instanceof View) {
            View v = (View) view;
            return forParentActivity(v).get(v, data, presenterClass);
        } else if (view instanceof Activity) {
            Activity a = (Activity) view;
            return forActivity(a).get(data, presenterClass);
        } else {
            throw new IllegalArgumentException("View has to be instance of android View or Activity.");
        }
    }

    public static void removePresentersFor(View view) {
        assert view != null;

        forParentActivity(view).removeFor(view);
    }

    public static void removePresentersFor(Activity activity) {
        assert activity != null;

        if (!activity.isFinishing()) {
            return;
        }

        Object activityId = buildActivityId(activity);
        getInstance().removeActivityPresenters(activityId);
    }

    public static void savePresentersFor(Activity activity, Bundle state) {
        assert activity != null;
        assert state != null;

        forActivity(activity).saveInto(state);
    }

    public static void restorePresentersFor(Activity activity, Bundle state) {
        assert activity != null;

        if (state == null) {
            return;
        }

        forActivity(activity).restoreFrom(state);
    }

    public static String getActivityId(Bundle state) {
        return (state != null) ? state.getString("blade:activity_id") : UUID.randomUUID().toString();
    }

    public static void putActivityId(Bundle state, String activityId) {
        state.putString("blade:activity_id", activityId);
    }

    // ------------------------------------------------------------------------------------

    private static ActivityPresenterManager forParentActivity(View view) {
        assert view != null;

        return forActivity((Activity) view.getContext());
    }

    private static ActivityPresenterManager forActivity(Activity activity) {
        assert activity != null;

        Object activityId = buildActivityId(activity);
        return getInstance().getActivityPresenters(activityId);
    }

    private static Object buildActivityId(Activity activity) {
        if (activity instanceof MvpActivity) {
            MvpActivity a = (MvpActivity) activity;
            return String.format("%s:%s", activity.getClass().getCanonicalName(), a.getId());
        } else {
            throw new IllegalStateException("Activity is missing @Blade annotation.");
        }
    }


    private static PresenterManager sInstance;

    private static PresenterManager getInstance() {
        if (sInstance == null) {
            sInstance = new PresenterManager();
        }
        return sInstance;
    }

    private final Map<Object, ActivityPresenterManager> mActivityPresenters;

    private PresenterManager() {
        mActivityPresenters = new HashMap<>();
    }

    private ActivityPresenterManager getActivityPresenters(Object activityId) {
        assert activityId != null;

        if (!mActivityPresenters.containsKey(activityId)) {
            mActivityPresenters.put(activityId, new ActivityPresenterManager());
        }

        return mActivityPresenters.get(activityId);
    }

    private void removeActivityPresenters(Object activityId) {
        ActivityPresenterManager apm = mActivityPresenters.remove(activityId);
        if (apm != null) {
            apm.removeAll();
        }
    }

    private static class ActivityPresenterManager {

        private final Map<Class, IPresenter> mActivityPresenters;
        private final Map<String, Map<Class, IPresenter>> mViewPresenters;
        private Bundle mState;

        private ActivityPresenterManager() {
            mActivityPresenters = new HashMap<>();
            mViewPresenters = new HashMap<>();
            mState = null;
        }

        public <V extends IView, D> void put(View view, D data, IPresenter<V, D> presenter) {
            assert view != null;
            assert data != null;
            assert presenter != null;

            String viewId = buildViewId(view, data);
            putViewPresenter(viewId, presenter);

            boolean restored = false;

            if (mState != null) {
                Bundle viewPresentersState = mState.getBundle(viewId);
                if (viewPresentersState != null) {
                    String key = presenter.getClass().getCanonicalName();
                    Bundle presenterState = viewPresentersState.getBundle(key);
                    if (presenterState != null) {
                        presenter.restoreState(presenterState);
                        restored = true;
                    }
                }
            }

            presenter.create(data, restored);
        }

        public <V extends IView, D> void put(D data, IPresenter<V, D> presenter) {
            assert data != null;
            assert presenter != null;

            putActivityPresenter(presenter);

            boolean restored = false;

            if (mState != null) {
                String key = presenter.getClass().getCanonicalName();
                Bundle presenterState = mState.getBundle(key);
                if (presenterState != null) {
                    presenter.restoreState(presenterState);
                    restored = true;
                }
            }

            presenter.create(data, restored);
        }

        public <D> IPresenter get(View view, D data, Class presenterClass) {
            assert view != null;
            assert data != null;
            assert presenterClass != null;

            String viewId = buildViewId(view, data);
            return getViewPresenter(viewId, presenterClass);
        }

        public <D> IPresenter get(D data, Class presenterClass) {
            assert data != null;
            assert presenterClass != null;

            return getActivityPresenter(presenterClass);
        }

        public void removeFor(View view) {
            assert view != null;

            if (view.getTag() == null) {
                return;
            }

            String viewId = buildViewId(view, view.getTag());
            Map<Class, IPresenter> presenters = mViewPresenters.get(viewId);
            if (presenters == null) {
                return;
            }

            for (IPresenter presenter : presenters.values()) {
                presenter.destroy();
            }
            presenters.clear();
            mViewPresenters.remove(viewId);
        }

        public void removeAll() {
            // activity presenters
            for (IPresenter presenter : mActivityPresenters.values()) {
                presenter.destroy();
            }
            mActivityPresenters.clear();

            // view presenters
            for (Map<Class, IPresenter> viewPresenters : mViewPresenters.values()) {
                for (IPresenter presenter : viewPresenters.values()) {
                    presenter.destroy();
                }
                viewPresenters.clear();
            }
            mViewPresenters.clear();
        }

        public void saveInto(Bundle state) {
            // save activity presenters
            save(mActivityPresenters, state);

            // save view presenters
            for (Map.Entry<String, Map<Class, IPresenter>> viewEntry : mViewPresenters.entrySet()) {
                String viewId = viewEntry.getKey();
                Map<Class, IPresenter> viewPresenters = viewEntry.getValue();
                Bundle viewPresentersState = new Bundle();

                save(viewPresenters, viewPresentersState);

                state.putBundle(viewId, viewPresentersState);
            }

            mState = state;
        }

        private void save(Map<Class, IPresenter> presenters, Bundle state) {
            for (Map.Entry<Class, IPresenter> presenterEntry : presenters.entrySet()) {
                String key = presenterEntry.getKey().getCanonicalName();
                IPresenter presenter = presenterEntry.getValue();
                Bundle presenterState = new Bundle();

                presenter.saveState(presenterState);

                state.putBundle(key, presenterState);
            }
        }

        public void restoreFrom(Bundle state) {
            mState = state;
        }

        private void putViewPresenter(String viewId, IPresenter presenter) {
            Map<Class, IPresenter> viewPresenters = mViewPresenters.get(viewId);
            if (viewPresenters == null) {
                viewPresenters = new HashMap<>();
                mViewPresenters.put(viewId, viewPresenters);
            }

            viewPresenters.put(presenter.getClass(), presenter);
        }

        private IPresenter getViewPresenter(String viewId, Class presenterClass) {
            Map<Class, IPresenter> viewPresenters = mViewPresenters.get(viewId);
            if (viewPresenters == null) {
                return null;
            }

            return viewPresenters.get(presenterClass);
        }

        private void putActivityPresenter(IPresenter presenter) {
            mActivityPresenters.put(presenter.getClass(), presenter);
        }

        private IPresenter getActivityPresenter(Class presenterClass) {
            return mActivityPresenters.get(presenterClass);
        }

        private String buildViewId(View view, Object tagObject) {
            return String.format("%s:%s", view.getClass().getCanonicalName(), tagObject.toString());
        }

    }

}
