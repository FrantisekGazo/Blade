package eu.f3rog.blade.sample.state;

import androidx.fragment.app.Fragment;
import blade.Arg;
import blade.State;

public class StateArgFragment extends Fragment {

    @Arg
    @State
    int num;
}
