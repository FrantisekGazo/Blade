package blade.mvp;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class {@link PresenterManager}
 *
 * @author FrantisekGazo
 * @version 2016-02-12
 */
public class PresenterManager {

    private static final String ACTIVITY_ID = "ACTIVITY-ID";

    public static void put(IView view, Object tagObject, IPresenter presenter) {
        assert view != null;
        assert tagObject != null;
        assert presenter != null;

        forParentActivity(view).put(view, tagObject, presenter);
    }

    public static IPresenter get(IView view, Object tagObject, Class presenterClass) {
        assert view != null;
        assert tagObject != null;
        assert presenterClass != null;

        return forParentActivity(view).get(view, tagObject, presenterClass);
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
        apm.removeAll();
    }

    private static class ActivityPresenterManager {

        private final Map<String, IPresenter> mPresenters;

        private ActivityPresenterManager() {
            mPresenters = new HashMap<>();
        }

        public void put(IView view, Object tagObject, IPresenter presenter) {
            assert view != null;
            assert tagObject != null;
            assert presenter != null;

            String id = buildId(view, tagObject, presenter.getClass());

            mPresenters.put(id, presenter);
        }

        public IPresenter get(IView view, Object tagObject, Class presenterClass) {
            assert view != null;
            assert tagObject != null;
            assert presenterClass != null;

            String id = buildId(view, tagObject, presenterClass);

            return mPresenters.get(id);
        }

        private String buildId(IView view, Object tagObject, Class presenterClass) {
            return view.getClass().getCanonicalName() + ":" + tagObject.toString() + ":" + presenterClass.getCanonicalName();
        }

        public void removeFor(IView view) {
            assert view != null;

            String keyStart = view.getClass() + ":" + view.getTag() + ":";
            List<String> toRemove = new ArrayList<>();

            // TODO : optimize !!!

            for (String key : mPresenters.keySet()) {
                if (key.startsWith(keyStart)) {
                    toRemove.add(key);
                }
            }

            for (int i = 0, c = toRemove.size(); i < c; i++) {
                String key = toRemove.get(i);
                IPresenter presenter = mPresenters.remove(key);
                presenter.destroy();
            }
        }

        public void removeAll() {
            for (IPresenter presenter : mPresenters.values()) {
                presenter.destroy();
            }
            mPresenters.clear();
        }
    }

}
