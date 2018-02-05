package com.intendia.reactivity.client;

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
        @Binds @Singleton PlaceManager bindPlaceManager(PlaceManagerTestUtil o);
        @Provides @Singleton static PlaceManagerWindowMethodsTestUtil providePlaceManagerWindowMethodsTestUtil() {
            return mock(PlaceManagerWindowMethodsTestUtil.class);
        }
        @Provides static View provideView() { return mock(View.class); }
        @Provides @Singleton static TestScheduler provideTestScheduler() { return new TestScheduler(); }

        @Binds @IntoSet Proxy<? extends PresenterChild<?>> bindHomeProxy(DummyProxyPlaceWithDenyGatekeeper o);
        @Binds @IntoSet Proxy<? extends PresenterChild<?>> bindAboutUsProxy(DummyProxyPlaceWithGrantGatekeeper o);
        @Binds @IntoSet Proxy<? extends PresenterChild<?>> bindContactProxy(DummyProxyPlaceDefault o);

        @Binds @Named("DenyGatekeeper") Gatekeeper.Factory bindDenyGatekeeper(DenyGatekeeper o);
        @Binds @Named("GrantGatekeeper") Gatekeeper.Factory bindGrantGatekeeper(GrantGatekeeper o);
    }

    @Singleton @Component(modules = MyModule.class) interface MyComponent {
        MembersInjector<GatekeeperTest> injector();
    }

    //@formatter:off
    //@TestMockSingleton
     static class DummyPresenterWithDenyGatekeeper extends PresenterChild<View> { // DummyProxyPlaceWithDenyGatekeeper
        @Inject DummyPresenterWithDenyGatekeeper(View v, RootContentSlot root) { super(v, root); }
        @Override public final boolean isVisible() { return super.isVisible(); }
    }
    //@TestEagerSingleton
    static class DummyProxyPlaceWithDenyGatekeeper extends Proxy<DummyPresenterWithDenyGatekeeper> {
        @Inject DummyProxyPlaceWithDenyGatekeeper(Provider<DummyPresenterWithDenyGatekeeper> p, EventBus bus,
                @Named("DenyGatekeeper") Gatekeeper.Factory g) {
            super(p, bus, new Place("deny", g.create()));
        }
    }

    //@TestMockSingleton
     static class DummyPresenterWithGrantGatekeeper extends PresenterChild<View> { //DummyProxyPlaceWithGrantGatekeeper
        @Inject DummyPresenterWithGrantGatekeeper(View v, RootContentSlot root) { super(v, root); }
        @Override public final boolean isVisible() { return super.isVisible(); }
    }
    //@TestEagerSingleton
    static class DummyProxyPlaceWithGrantGatekeeper extends Proxy<DummyPresenterWithGrantGatekeeper> {
        @Inject DummyProxyPlaceWithGrantGatekeeper(Provider<DummyPresenterWithGrantGatekeeper> p, EventBus bus,
                @Named("GrantGatekeeper") Gatekeeper.Factory g) {
            super(p, bus, new Place("grant", g.create()));
        }
    }

    //@TestMockSingleton
    static class DummyPresenterDefault extends PresenterChild<View> { //DummyProxyPlaceDefault
        @Inject DummyPresenterDefault(View v, RootContentSlot root) { super(v, root); }
        @Override public final boolean isVisible() { return super.isVisible(); }
    }
    //@TestEagerSingleton
    static class DummyProxyPlaceDefault extends  Proxy<DummyPresenterDefault> {
        @Inject DummyProxyPlaceDefault(Provider<DummyPresenterDefault> p, EventBus bus) {
            super(p, bus, new Place("defaultPlace"));
        }
    }
    //@formatter:on

    static class DenyGatekeeper implements Gatekeeper.Factory {
        @Inject public DenyGatekeeper() {}
        @Override public Gatekeeper create(String... params) { return request -> false; }
    }

    static class GrantGatekeeper implements Gatekeeper.Factory {
        @Inject public GrantGatekeeper() {}
        @Override public Gatekeeper create(String... params) { return request -> true; }
    }

    @Before public void prepare() {
        DaggerGatekeeperTest_MyComponent.create().injector().injectMembers(this);
    }

    // SUT
    @Inject PlaceManager placeManager;
    @Inject TestScheduler deferredCommandManager;
    @Inject DummyPresenterDefault defaultPresenter;
    @Inject DummyPresenterWithGrantGatekeeper presenterWithGatekeeper;

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
        verify(defaultPresenter).forceReveal();
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
        verify(presenterWithGatekeeper).forceReveal();
    }
}
