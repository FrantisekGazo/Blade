package blade.mvp;

/**
 * Interface {@link IPresenter}.
 * Class that implements this interface has to have default constructor.
 *
 * @param <V> View type
 * @param <D> Data type
 */
public interface IPresenter<V extends IView, D> {

    void create(D data);

    void destroy();

    void bind(V view);

    void unbind();

    void saveState(Object bundle);

    void stateRestored();

}
