package blade.mvp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface {@link IPresenter}.
 * Class that implements this interface has to have default constructor.
 *
 * @param <V> View type
 */
public interface IPresenter<V extends IView> {

    /**
     * Called only when presenter is firstly created with null state and also when presenter needs to be restored from state.
     */
    void onCreate(@Nullable Object state);

    /**
     * Saves state to given state object.
     * <p/>
     * Normally this would be an instance of android's Bundle class, but in order to allow unit testing there is just Object as parameter type.
     */
    void onSaveState(@Nonnull Object state);

    /**
     * Called before destroying this presenter.
     */
    void onDestroy();

    /**
     * Called every time this Presenter is connecting to a view.
     *
     * @param view View
     */
    void onBind(@Nonnull V view);

    @Nullable
    V getView();

    /**
     * Called every time this Presenter is disconnecting from a view.
     */
    void onUnbind();

}
