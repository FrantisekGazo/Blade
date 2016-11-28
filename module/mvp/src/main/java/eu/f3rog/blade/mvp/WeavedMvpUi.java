package eu.f3rog.blade.mvp;

import android.os.Bundle;


/**
 * Interface {@link WeavedMvpUi}
 * <p/>
 * NOTE: This class is only for internal usage of this library.
 *
 * @author FrantisekGazo
 */
public interface WeavedMvpUi {

    String getWeavedId();

    void setWeavedId(String id);

    Bundle getWeavedState();

    void setWeavedState(Bundle state);

    boolean wasOnSaveCalled();

    void setOnSaveCalled();
}

