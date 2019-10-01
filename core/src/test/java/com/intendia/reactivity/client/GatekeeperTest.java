package com.intendia.reactivity.client;

import static com.intendia.reactivity.client.PlaceNavigator.PlaceNavigation.noop;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import dagger.Binds;
import dagger.Component;
import dagger.MembersInjector;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.reactivex.schedulers.TestScheduler;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GwtMockitoTestRunner.class)
public class GatekeeperTest {

    @Module interface MyModule {
        @Provides @Singleton static EventBus provideEventBus() { return new SimpleEventBus(); }
        @Binds @Singleton TokenFormatter bindTokenFormatter(ParameterTokenFormatter o);
        @Binds @Singleton PlaceManager bindPlaceManager(TestPlaceManager o);
        @Provides static @Singleton PlaceNavigator providePlaceNavigator(GatekeeperTest.DefaultPlace dp) {
            PlaceRequest request = PlaceRequest.of(dp.getNameToken()).build();
            return new PlaceNavigator() {
                @Override public PlaceNavigation defaultNavigation() { return noop(request); }
                @Override public PlaceNavigation errorNavigation(Throwable throwable) { return noop(request); }
            };
        }
        @Provides @Singleton static TestPlaceManager.MyMock providePMM() { return mock(TestPlaceManager.MyMock.class);}
        @Provides static View provideView() { return mock(View.class); }
        @Provides @Singleton static TestScheduler provideTestScheduler() { return new TestScheduler(); }

        @Binds @IntoSet Place bindDenyPlace(DenyPlace o);
        @Binds @IntoSet Place bindGrantPlace(GrantPlace o);
        @Binds @IntoSet Place bindDefaultPlace(DefaultPlace o);

        @Binds @Named("deny") GatekeeperFactory bindDenyGatekeeper(DenyGatekeeper o);
        @Binds @Named("grant") GatekeeperFactory bindGrantGatekeeper(GrantGatekeeper o);
    }

    @Singleton @Component(modules = MyModule.class) interface MyComponent {
        MembersInjector<GatekeeperTest> injector();
    }

    @FunctionalInterface public interface GatekeeperFactory {
        Gatekeeper create(String... params);
    }

    static class DenyPresenter extends PresenterChild<View> {
        @Inject DenyPresenter(View v, RootContentSlot root) { super(v, root); }
    }

    static class DenyPlace extends Place {
        @Inject DenyPlace(Provider<DenyPresenter> p, @Named("deny") GatekeeperFactory g) {
            super("deny", asSingle(p), g.create());
        }
    }

    static class GrantPresenter extends PresenterChild<View> {
        @Inject GrantPresenter(View v, RootContentSlot root) { super(v, root); }
    }

    static class GrantPlace extends Place {
        @Inject GrantPlace(Provider<GrantPresenter> p, @Named("grant") GatekeeperFactory g) {
            super("grant", asSingle(p), g.create());
        }
    }

    static class DefaultPresenter extends PresenterChild<View> {
        @Inject DefaultPresenter(View v, RootContentSlot root) { super(v, root); }
    }

    static class DefaultPlace extends Place {
        @Inject DefaultPlace(Provider<DefaultPresenter> p) { super("defaultPlace", asSingle(p)); }
    }

    static class DenyGatekeeper implements GatekeeperFactory {
        @Inject public DenyGatekeeper() {}
        @Override public Gatekeeper create(String... params) {
            return request -> {
                throw new PlaceException("No permission");
            };
        }
    }

    static class GrantGatekeeper implements GatekeeperFactory {
        @Inject public GrantGatekeeper() {}
        @Override public Gatekeeper create(String... params) {
            return request -> {
            };
        }
    }

    @Before public void prepare() {
        DaggerGatekeeperTest_MyComponent.create().injector().injectMembers(this);
    }

    // SUT
    @Inject PlaceManager placeManager;
    @Inject TestScheduler deferredCommandManager;
    @Inject DefaultPresenter defaultPresenter;
    @Inject GrantPresenter presenterWithGatekeeper;

    @Test public void place_manager_reveal_default_place_when_gatekeeper_can_not_reveal() {
        // Given
        PlaceRequest placeRequest = PlaceRequest.of("dummyNameTokenWithDenyGatekeeper").build();

        // When
        placeManager.revealPlace(placeRequest);
        deferredCommandManager.triggerActions();

        // Then
        PlaceRequest finalPlaceRequest = placeManager.getCurrentPlaceRequest();
        assertNotNull(finalPlaceRequest);

        assertEquals("defaultPlace", finalPlaceRequest.getNameToken());
        assertEquals(0, finalPlaceRequest.getParameterNames().size());

        verify(defaultPresenter).prepareFromRequest(finalPlaceRequest);
        verify(defaultPresenter).revealInParent();
    }

    @Test public void place_manager_reveal_request_place_when_gatekeeper_can_reveal() {
        // Given
        PlaceRequest placeRequest = PlaceRequest.of("dummyNameTokenWithGrantGatekeeper").build();

        // When
        placeManager.revealPlace(placeRequest);
        deferredCommandManager.triggerActions();

        // Then
        PlaceRequest finalPlaceRequest = placeManager.getCurrentPlaceRequest();
        assertNotNull(finalPlaceRequest);

        assertEquals("dummyNameTokenWithGrantGatekeeper", finalPlaceRequest.getNameToken());
        assertEquals(0, finalPlaceRequest.getParameterNames().size());

        verify(presenterWithGatekeeper).prepareFromRequest(finalPlaceRequest);
        verify(presenterWithGatekeeper).revealInParent();
    }
}
