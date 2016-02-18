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

    <T extends V> void bind(T view);

    void unbind();

    void saveState(Object bundle);

    void stateRestored();

}
