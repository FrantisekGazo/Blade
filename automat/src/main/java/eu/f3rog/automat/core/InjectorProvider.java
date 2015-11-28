package eu.f3rog.automat.core;

import java.util.HashMap;
import java.util.Map;

public class InjectorProvider {

    private Map<Class, Injector> mInjectors = new HashMap<>();
    /*
    public <I extends Injector> I get(Class<I> injectorClass) {
        I injector;
        if (mInjectors.containsKey(injectorClass)) {
            injector = (I) mInjectors.get(injectorClass);
        } else {
            injector = injectorClass.newInstance();
        }
        return injector;
    }
*/
}
