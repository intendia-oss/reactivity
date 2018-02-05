package com.intendia.reactivity.client;

import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import com.intendia.reactivity.client.Slots.SingleSlot;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(GwtMockitoTestRunner.class)
public class InjectionTest {

    // mocking presenter test

    @Module interface MockModule {
        @Provides @Singleton static EventBus bindEventBus() { return new SimpleEventBus(); }
        @Provides @Singleton @Named("Sub") static PresenterWidget<?> provideSub() {
            return Mockito.mock(PresenterWidget.class);
        }
    }

    @Singleton @Component(modules = MockModule.class) interface MockComponent {
        MainPresenter mainPresenter();
    }

    @Test public void settingMockSubPresenterShouldNotCrash() {
        DaggerInjectionTest_MockComponent.create().mainPresenter().setSubPresenter();
    }

    // real presenter test

    @Module interface RealModule {
        @Provides @Singleton static EventBus bindEventBus() { return new SimpleEventBus(); }
        @Provides @Singleton @Named("Sub") static PresenterWidget<?> provideSub() {
            return new SubPresenter(Mockito.mock(SubPresenter.MyView.class));
        }
    }

    @Singleton @Component(modules = RealModule.class) interface RealComponent {
        MainPresenter mainPresenter();
    }

    @Test public void settingRealSubPresenterShouldNotCrash() {
        DaggerInjectionTest_RealComponent.create().mainPresenter().setSubPresenter();
    }

    // presenters

    static class MainPresenter extends PresenterChild<MainPresenter.MyView> {
        static final SingleSlot<PresenterWidget<?>> SLOT_SetMainContent = new SingleSlot<>();

        static class MyView extends CompositeView {
            @Inject MyView() { bindSlot(SLOT_SetMainContent, (HasOneWidget) new SimplePanel()); }
        }

        private final PresenterWidget<?> subPresenter;

        @Inject MainPresenter(MyView v, RootContentSlot root, @Named("Sub") PresenterWidget<?> subPresenter) {
            super(v, root);
            this.subPresenter = subPresenter;
        }

        void setSubPresenter() { setInSlot(SLOT_SetMainContent, subPresenter); }
    }

    static class SubPresenter extends PresenterWidget<SubPresenter.MyView> {
        static class MyView extends CompositeView {
            @Inject MyView() {}
        }
        @Inject SubPresenter(MyView view) { super(view); }
    }
}
