package blade.mvp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class {@link BasePresenter}
 *
 * @author FrantisekGazo
 */
public abstract class BasePresenter<V extends IView>
        implements IPresenter<V> {

    private V mView;

    @Override
    @Nullable
    public V getView() {
        return mView;
    }

    @Override
    public void onCreate(@Nullable Object state) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onBind(@Nonnull V view) {
        mView = view;
    }

    @Override
    public void onUnbind() {
        mView = null;
    }

    @Override
    public void onSaveState(@Nonnull Object state) {
    }
}
