package blade.mvp;

/**
 * Interface {@link IView}.
 */
public interface IView {

    /**
     * Returns context in which this view is.
     * <p/>
     * Normally this would be an instance of android's Activity class, but in order to allow unit testing there is just Object as return type.
     */
    Object getContext();

    /**
     * Returns a tag of this view.
     */
    Object getTag();

}

