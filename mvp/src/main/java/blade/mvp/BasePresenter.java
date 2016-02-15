package blade.mvp;

import java.lang.ref.WeakReference;

/**
 * Class {@link BasePresenter}
 *
 * @author FrantisekGazo
 * @version 2016-02-14
 */
public abstract class BasePresenter<V extends IView, D>
        implements IPresenter<V, D> {

    private WeakReference<V> mView;

    public V getView() {
        return (mView != null) ? mView.get() : null;
    }

    @Override
    public void create(D data) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public <T extends V> void bind(T view) {
        mView = new WeakReference<V>(view);
    }

    @Override
    public void unbind() {
        mView = null;
    }

    @Override
    public void saveState(Object bundle) {

    }

    @Override
    public void stateRestored() {

    }

}
