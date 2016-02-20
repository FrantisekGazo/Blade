package blade.mvp;

import android.app.Activity;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Class {@link PresenterManager}
 *
 * @author FrantisekGazo
 * @version 2016-02-12
 */
public class PresenterManager {

    private static final String ACTIVITY_ID = "ACTIVITY-ID";

    public static <V extends IView, D> void put(V view, D data, IPresenter<V, D> presenter) {
        assert view != null;
        assert data != null;
        assert presenter != null;

        forParentActivity(view).put(view, data, presenter);
    }

    public static IPresenter get(IView view, Object data, Class presenterClass) {
        assert view != null;
        assert data != null;
        assert presenterClass != null;

        return forParentActivity(view).get(view, data, presenterClass);
    }

    public static void removePresentersFor(IView view) {
        assert view != null;

        forParentActivity(view).removeFor(view);
    }

    public static void removePresentersFor(Activity activity) {
        assert activity != null;

        Object activityId = buildActivityId(activity);
        getInstance().removeActivityPresenters(activityId);
    }

    public static void savePresentersFor(Activity activity, Bundle state) {
        assert activity != null;
        assert state != null;

        Object activityId = buildActivityId(activity);
        ActivityPresenterManager presenters = getInstance().getActivityPresenters(activityId);
        presenters.saveInto(state);
    }

    public static void restorePresentersFor(Activity activity, Bundle state) {
        assert activity != null;
        assert state != null;

        Object activityId = buildActivityId(activity);
        ActivityPresenterManager presenters = getInstance().getActivityPresenters(activityId);
        presenters.restoreFrom(state);
    }

    // ------------------------------------------------------------------------------------

    private static ActivityPresenterManager forParentActivity(IView view) {
        assert view != null;

        Object activityId = buildActivityId((Activity) view.getContext());
        return getInstance().getActivityPresenters(activityId);
    }

    private static ActivityPresenterManager forActivity(Activity activity) {
        assert activity != null;

        Object activityId = buildActivityId(activity);
        return getInstance().getActivityPresenters(activityId);
    }

    private static Object buildActivityId(Activity activity) {
        Object id = activity.getSystemService(ACTIVITY_ID);
        if (id == null) {
            throw new IllegalStateException("Activity is missing @Blade annotation.");
        }
        return activity.getClass().getCanonicalName() + ":" + id;
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

        private final Map<String, Map<String, IPresenter>> mPresenters;
        private Bundle mState;

        private ActivityPresenterManager() {
            mPresenters = new HashMap<>();
            mState = null;
        }

        public <V extends IView, D> void put(V view, D data, IPresenter<V, D> presenter) {
            assert view != null;
            assert data != null;
            assert presenter != null;

            String viewId = buildViewId(view, data);
            putPresenter(viewId, presenter);

            boolean restored = false;

            if (mState != null) {
                Bundle viewPresentersState = mState.getBundle(viewId);
                if (viewPresentersState != null) {
                    String presenterId = buildPresenterId(presenter.getClass());
                    Bundle presenterState = viewPresentersState.getBundle(presenterId);
                    if (presenterState != null) {
                        presenter.restoreState(presenterState);
                        restored = true;
                    }
                }
            }

            presenter.create(data, restored);
        }

        public IPresenter get(IView view, Object data, Class presenterClass) {
            assert view != null;
            assert data != null;
            assert presenterClass != null;

            String viewId = buildViewId(view, data);
            return getPresenter(viewId, presenterClass);
        }

        public void removeFor(IView view) {
            assert view != null;

            if (view.getTag() == null) {
                return;
            }

            String viewId = buildViewId(view, view.getTag());
            Map<String, IPresenter> presenters = mPresenters.get(viewId);
            if (presenters == null) {
                return;
            }

            for (IPresenter presenter : presenters.values()) {
                presenter.destroy();
            }
            presenters.clear();
            mPresenters.remove(viewId);
        }

        public void removeAll() {
            for (Map<String, IPresenter> viewPresenters : mPresenters.values()) {
                for (IPresenter presenter : viewPresenters.values()) {
                    presenter.destroy();
                }
                viewPresenters.clear();
            }
            mPresenters.clear();
        }

        public void saveInto(Bundle state) {
            for (Map.Entry<String, Map<String, IPresenter>> viewEntry : mPresenters.entrySet()) {
                String viewId = viewEntry.getKey();
                Map<String, IPresenter> viewPresenters = viewEntry.getValue();
                Bundle viewPresentersState = new Bundle();

                for (Map.Entry<String, IPresenter> presenterEntry : viewPresenters.entrySet()) {
                    String key = presenterEntry.getKey();
                    IPresenter presenter = presenterEntry.getValue();
                    Bundle presenterState = new Bundle();

                    presenter.saveState(presenterState);

                    viewPresentersState.putBundle(key, presenterState);
                }

                state.putBundle(viewId, viewPresentersState);
            }
            mState = state;
        }

        public void restoreFrom(Bundle state) {
            mState = state;
        }

        private void putPresenter(String viewId, IPresenter presenter) {
            Map<String, IPresenter> viewPresenters = mPresenters.get(viewId);
            if (viewPresenters == null) {
                viewPresenters = new HashMap<>();
                mPresenters.put(viewId, viewPresenters);
            }

            String presenterId = buildPresenterId(presenter.getClass());
            viewPresenters.put(presenterId, presenter);
        }

        private IPresenter getPresenter(String viewId, Class presenterClass) {
            Map<String, IPresenter> viewPresenters = mPresenters.get(viewId);
            if (viewPresenters == null) {
                return null;
            }

            String presenterId = buildPresenterId(presenterClass);
            return viewPresenters.get(presenterId);
        }

        private String buildPresenterId(Class presenterClass) {
            return presenterClass.getCanonicalName();
        }

        private String buildViewId(IView view, Object tagObject) {
            return String.format("%s:%s", view.getClass().getCanonicalName(), tagObject.toString());
        }

    }

}
