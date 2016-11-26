package eu.f3rog.blade.mvp;

import android.os.Bundle;

/**
 * Interface {@link WeavedMvpView}
 *
 * @author FrantisekGazo
 */
public interface WeavedMvpView {

    String getWeavedId();

    void setWeavedId(String id);

    Bundle getWeavedState();

    void setWeavedState(Bundle state);

    boolean wasOnSaveCalled();

    void setOnSaveCalled();
}

