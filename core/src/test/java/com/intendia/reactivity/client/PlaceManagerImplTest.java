package com.intendia.reactivity.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.intendia.reactivity.client.PlaceManager.NavigationEvent;
import com.intendia.reactivity.client.PlaceManager.NavigationEvent.NavigationHandler;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import dagger.Binds;
import dagger.Component;
import dagger.MembersInjector;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.reactivex.Completable;
import io.reactivex.schedulers.TestScheduler;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GwtMockitoTestRunner.class)
public class PlaceManagerImplTest {

    @Module interface MyModule {
        @Provides @Singleton static EventBus provideEventBus() { return new SimpleEventBus(); }
        @Binds @Singleton TokenFormatter bindTokenFormatter(ParameterTokenFormatter o);
        @Binds @Singleton PlaceManager bindPlaceManager(PlaceManagerTestUtil o);
        @Provides @Singleton static PlaceManagerWindowMethodsTestUtil providePlaceManagerWindowMethodsTestUtil() {
            return mock(PlaceManagerWindowMethodsTestUtil.class);
        }
        @Provides static View provideView() { return mock(View.class); }
        @Provides @Singleton static TestScheduler provideTestScheduler() { return new TestScheduler(); }

        @Binds @IntoSet Proxy<? extends PresenterChild<?>> bindBasicProxy(DummyProxyPlaceBasic o);
        @Binds @IntoSet Proxy<? extends PresenterChild<?>> bindRedirectProxy(DummyProxyPlaceRedirect o);
        @Binds @IntoSet Proxy<? extends PresenterChild<?>> bindRedirectNoHistoryProxy(
                DummyProxyPlaceRedirectNoHistory o);
    }

    @Singleton @Component(modules = GatekeeperTest.MyModule.class) interface MyComponent {
        MembersInjector<PlaceManagerImplTest> injector();
    }

    // @TestMockSingleton
    static class DummyPresenterBasic extends PresenterChild<View> { //DummyProxyPlaceBasic
        @Inject DummyPresenterBasic(View v, RootContentSlot root) { super(v, root); }
        @Override public final boolean isVisible() { return super.isVisible(); }
    }

    // @TestEagerSingleton
    static class DummyProxyPlaceBasic extends Proxy<DummyPresenterBasic> {
        @Inject DummyProxyPlaceBasic(Provider<DummyPresenterBasic> p, EventBus bus) {
            super(p, bus, new Place("dummyNameTokenBasic"));
        }
    }

    /** This presenter automatically redirects in prepareFromRequest to PresenterBasic. */
    // @TestEagerSingleton
    static class DummyPresenterRedirect extends PresenterChild<View> { //DummyProxyPlaceBasic
        private final PlaceManager placeManager;
        public PlaceRequest preparedRequest;
        public int prepareFromRequestCalls;
        public int revealInParentCalls;

        @Inject DummyPresenterRedirect(RootContentSlot root, PlaceManager placeManager) {
            super(mock(View.class), root);
            this.placeManager = placeManager;
        }

        @Override public Completable prepareFromRequest(PlaceRequest request) {
            super.prepareFromRequest(request);
            ++prepareFromRequestCalls;
            preparedRequest = request;
            placeManager.revealPlace(PlaceRequest.of("dummyNameTokenBasic").build());
            return Completable.never();
        }

        @Override protected void revealInParent() { ++revealInParentCalls; }
    }

    // @TestEagerSingleton
    static class DummyProxyPlaceRedirect extends Proxy<DummyPresenterRedirect> {
        @Inject DummyProxyPlaceRedirect(Provider<DummyPresenterRedirect> p, EventBus bus) {
            super(p, bus, new Place("dummyNameTokenRedirect"));
        }
    }

    // @TestEagerSingleton
    static class DummyPresenterRedirectNoHistory extends PresenterChild<View> { //DummyProxyPlaceRedirectNoHistory
        private final PlaceManager placeManager;
        private static final String TOKEN = "dummyNameTokenRedirectNoHistory";

        @Inject DummyPresenterRedirectNoHistory(RootContentSlot root, PlaceManager placeManager) {
            super(mock(View.class), root);
            this.placeManager = placeManager;
        }

        @Override public Completable prepareFromRequest(PlaceRequest request) {
            super.prepareFromRequest(request);
            placeManager.revealPlace(PlaceRequest.of("dummyNameTokenBasic").build(), false);
            return Completable.never();
        }

        @Override protected void revealInParent() {}
    }

    // @TestEagerSingleton
    static class DummyProxyPlaceRedirectNoHistory extends Proxy<DummyPresenterRedirectNoHistory> {
        @Inject DummyProxyPlaceRedirectNoHistory(Provider<DummyPresenterRedirectNoHistory> p, EventBus bus) {
            super(p, bus, new Place(DummyPresenterRedirectNoHistory.TOKEN));
        }
    }

    // @TestSingleton
    static class NavigationEventSpy implements NavigationHandler {
        int navCount;
        NavigationEvent lastEvent;
        @Inject public NavigationEventSpy() {}
        public void onNavigation(NavigationEvent navigationEvent) {
            navCount++;
            lastEvent = navigationEvent;
        }
    }

    @Before public void prepare() {
        DaggerPlaceManagerImplTest_MyComponent.create().injector().injectMembers(this);
    }

    // SUT
    @Inject PlaceManager placeManager;
    @Inject PlaceManagerWindowMethodsTestUtil gwtWindowMethods;
    @Inject NavigationEventSpy navigationHandler;
    @Inject EventBus eventBus;
    @Inject DummyPresenterBasic presenterBasic;
    @Inject DummyPresenterRedirect presenterRedirect;

    @Test public void placeManagerRevealPlaceStandard() {
        // Given
        eventBus.addHandler(NavigationEvent.TYPE, navigationHandler);

        // When
        placeManager.revealPlace(PlaceRequest.of("dummyNameTokenBasic").with("dummyParam", "dummyValue").build());

        // Then
        PlaceRequest placeRequest = placeManager.getCurrentPlaceRequest();
        assertNotNull(placeRequest);

        assertEquals("dummyNameTokenBasic", placeRequest.getNameToken());
        assertEquals(1, placeRequest.getParameterNames().size());
        assertEquals("dummyValue", placeRequest.getParameter("dummyParam", null));

        verify(presenterBasic).prepareFromRequest(placeRequest);
        verify(presenterBasic).forceReveal();

        verify(gwtWindowMethods).setBrowserHistoryToken(any(String.class), eq(false));

        assertEquals(1, navigationHandler.navCount);
        placeRequest = navigationHandler.lastEvent.getRequest();
        assertEquals("dummyNameTokenBasic", placeRequest.getNameToken());
        assertEquals(1, placeRequest.getParameterNames().size());
        assertEquals("dummyValue", placeRequest.getParameter("dummyParam", null));
    }

    /**
     * DummyPresenterRedirectNoHistory makes a call to revealPlace in prepareFromRequest. This call
     * is deferred but useBrowserUrl must be preserved and the history token must be set only once.
     */
    @Test public void placeManagerRevealPlaceRedirectInPrepareFromRequestNoHistory() {
        // Given
        PlaceRequest placeRequest = PlaceRequest.of(DummyPresenterRedirectNoHistory.TOKEN).build();

        // When
        placeManager.revealPlace(placeRequest);

        // Then
        verify(gwtWindowMethods, times(1)).setBrowserHistoryToken(any(String.class), eq(false));

        PlaceRequest finalPlaceRequest = placeManager.getCurrentPlaceRequest();
        assertEquals("dummyNameTokenBasic", finalPlaceRequest.getNameToken());
    }

    @Test public void placeManagerRevealPlaceRedirectInPrepareFromRequest() {
        // Given
        PlaceRequest placeRequest = PlaceRequest.of("dummyNameTokenRedirect").with("dummyParam", "dummyValue").build();

        // When
        placeManager.revealPlace(placeRequest);

        // Then
        PlaceRequest finalPlaceRequest = placeManager.getCurrentPlaceRequest();
        assertNotNull(finalPlaceRequest);

        assertEquals("dummyNameTokenBasic", finalPlaceRequest.getNameToken());
        assertEquals(0, finalPlaceRequest.getParameterNames().size());

        assertEquals(1, presenterRedirect.prepareFromRequestCalls);
        assertEquals(placeRequest, presenterRedirect.preparedRequest);
        assertEquals(0, presenterRedirect.revealInParentCalls);

        verify(presenterBasic).prepareFromRequest(finalPlaceRequest);
        verify(presenterBasic).forceReveal();
    }

    @Test public void placeManagerUserCallUpdateHistoryWhenRevealingPlace() {
        // When
        placeManager.revealPlace(PlaceRequest.of("dummyNameToken").with("dummyParam", "dummyValue").build());

        // Then
        PlaceRequest placeRequest = placeManager.getCurrentPlaceRequest();
        assertEquals("dummyNameToken", placeRequest.getNameToken());
        assertEquals(1, placeRequest.getParameterNames().size());
        assertEquals("dummyValue", placeRequest.getParameter("dummyParam", null));

        verify(gwtWindowMethods).setBrowserHistoryToken(any(String.class), eq(false));
    }

    @Test public void placeManagerRevealDefaultPlace() {
        // When
        placeManager.revealDefaultPlace();

        // Then
        PlaceRequest placeRequest = placeManager.getCurrentPlaceRequest();
        assertEquals("defaultPlace", placeRequest.getNameToken());
        assertEquals(0, placeRequest.getParameterNames().size());

        verify(gwtWindowMethods).setBrowserHistoryToken(any(String.class), eq(false));
    }
}
