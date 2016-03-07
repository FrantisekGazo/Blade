package eu.f3rog.blade.sample.mvp.di.q;

import javax.inject.Qualifier;

public interface SchedulerType {

    @Qualifier
    @interface Main {
    }

    @Qualifier
    @interface Task {
    }

}

