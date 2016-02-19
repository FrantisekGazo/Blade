package blade.mvp;

/**
 * Interface {@link IPresenter}.
 * Class that implements this interface has to have default constructor.
 *
 * @param <V> View type
 * @param <D> Data type
 */
public interface IPresenter<V extends IView, D> {

    void create(D data, boolean wasRestored);

    void destroy();

    void bind(V view);

    void unbind();

    /**
     * Saves state to given state object.
     * <p/>
     * Normally this would be an instance of android's Bundle class, but in order to allow unit testing there is just Object as parameter type.
     */
    void saveState(Object state);

    /**
     * Restores state from given state object.
     * <p/>
     * Normally this would be an instance of android's Bundle class, but in order to allow unit testing there is just Object as parameter type.
     */
    void restoreState(Object state);

}
