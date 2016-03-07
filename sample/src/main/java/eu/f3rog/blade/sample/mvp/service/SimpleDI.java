package eu.f3rog.blade.sample.mvp.service;

import rx.Scheduler;

public interface SimpleDI {

    Scheduler getMainScheduler();

    Scheduler getBackgroundScheduler();

}
