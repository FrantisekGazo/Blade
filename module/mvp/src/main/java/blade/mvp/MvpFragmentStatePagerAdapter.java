package blade.mvp;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;


/**
 * This class extends {@link FragmentStatePagerAdapter}. It removes the Presenters once the
 * pager item is destroyed ({@link #destroyItem(ViewGroup, int, Object)}). The presenter state
 * is stored and then restored once you return back to this pager item and {@link #instantiateItem(ViewGroup, int)}
 * is called.
 */
public abstract class MvpFragmentStatePagerAdapter
        extends FragmentStatePagerAdapter {

    public MvpFragmentStatePagerAdapter(@NonNull final FragmentManager fm) {
        super(fm);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);

        if (object instanceof IView) {
            IView view = (IView) object;
            PresenterManager.removeAllFor(view);
        }
    }
}
