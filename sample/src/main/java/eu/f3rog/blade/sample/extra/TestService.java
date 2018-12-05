package eu.f3rog.blade.sample.extra;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import blade.Extra;

/**
 * Class {@link TestService}
 *
 * @author FrantisekGazo
 * @version 2016-01-02
 */
public class TestService extends Service {

    @Extra
    String text;
    @Extra
    boolean flag;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
