package eu.f3rog.blade.sample.arg;

import android.support.v4.app.Fragment;

import blade.Arg;
import blade.State;

/**
 * Class {@link BaseFragment}
 *
 * @author FrantisekGazo
 * @version 2015-12-01
 */
public abstract class BaseFragment extends Fragment {

    @Arg
    double number;

    @State
    String somethingStateful;

}
