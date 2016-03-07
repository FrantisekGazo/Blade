package eu.f3rog.blade.sample.extra;

import android.app.IntentService;
import android.content.Intent;

import blade.Extra;

/**
 * Class {@link TestIntentService}
 *
 * @author FrantisekGazo
 * @version 2016-01-02
 */
public class TestIntentService extends IntentService {

    @Extra
    String text;
    @Extra
    boolean flag;

    public TestIntentService() {
        super("Test");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
