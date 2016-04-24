package blade.mvp;

/**
 * Class {@link BasePresenter}
 *
 * @author FrantisekGazo
 * @version 2016-02-14
 */
public abstract class BasePresenter<V extends IView, D>
        implements IPresenter<V, D> {

    private V mView;

    public V getView() {
        return mView;
    }

    @Override
    public void create(D data, boolean wasRestored) {
    }

    @Override
    public void setData(D data) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void bind(V view) {
        mView = view;
    }

    @Override
    public void unbind() {
        mView = null;
    }

    @Override
    public void saveState(Object state) {
    }

    @Override
    public void restoreState(Object state) {
    }

}
