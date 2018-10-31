package eu.f3rog.blade.sample.mvp;

import androidx.annotation.NonNull;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import eu.f3rog.blade.sample.mvp.di.component.Component;
import eu.f3rog.blade.sample.mvp.di.module.app.AppModule;
import eu.f3rog.blade.sample.mvp.model.Actor;
import eu.f3rog.blade.sample.mvp.presenter.ActorListPresenter;
import eu.f3rog.blade.sample.mvp.view.ActorListView;
import rx.Emitter;
import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.functions.Action1;
import rx.observers.TestSubscriber;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;
import rx.schedulers.Schedulers;

/**
 * Class {@link ActorPresentersTest}
 *
 * @author FrantisekGazo
 */
public final class ActorPresentersTest {

    private static class MockArticleListView implements ActorListView {

        @NonNull
        private final Emitter<String> mEmitter;

        public MockArticleListView(@NonNull Emitter<String> emitter) {
            mEmitter = emitter;
        }

        @Override
        public void showProgress() {
            mEmitter.onNext("progress");
        }

        @Override
        public void showError(@NonNull String errorMessage) {
            mEmitter.onError(new Exception(errorMessage));
        }

        @Override
        public void show(@NonNull List<Actor> actors) {
            mEmitter.onNext("" + actors.size());
            mEmitter.onCompleted();
        }

        @Override
        public void gotoActorDetail(@NonNull Actor actor) {
        }

    }

    @Before
    public void setup() {
        RxJavaPlugins.getInstance().reset();
        RxJavaPlugins.getInstance().registerSchedulersHook(new RxJavaSchedulersHook() {
// if you use immediate on all => everything will be synchronous
//            @Override
//            public Scheduler getIOScheduler() {
//                return Schedulers.immediate();
//            }
//
//            @Override
//            public Scheduler getComputationScheduler() {
//                return Schedulers.immediate();
//            }
//
//            @Override
//            public Scheduler getNewThreadScheduler() {
//                return Schedulers.immediate();
//            }
        });

        // make sure immediate Scheduler is used instead of Android's main Scheduler
        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testThatBindWillLoadData() {
        final ActorListPresenter presenter = Component.createAppComponent(new AppModule(null)).actorListPresenter();

        // this should trigger data loading
        presenter.onCreate(null);

        // bind a mock view
        final Observable<String> viewObservable = Observable.fromEmitter(new Action1<Emitter<String>>() {
            @Override
            public void call(Emitter<String> emitter) {
                presenter.onBind(new MockArticleListView(emitter));
            }
        }, Emitter.BackpressureMode.BUFFER);

        // subscribe and wait for terminating event
        final TestSubscriber<String> subscriber = new TestSubscriber<>();
        viewObservable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        final List<String> onNextEvents = subscriber.getOnNextEvents();
        try {
            // loading takes longer => view will receive "progress"
            Assert.assertEquals(2, onNextEvents.size());
            subscriber.assertValues("progress", "50");
        } catch (AssertionFailedError e) {
            // loading is quick => view will receive directly the result
            Assert.assertEquals(1, onNextEvents.size());
            subscriber.assertValues("50");
        }
    }

}
