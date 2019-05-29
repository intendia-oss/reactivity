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
import com.intendia.reactivity.client.PlaceManager.UpdateBrowserUrl;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import com.intendia.reactivity.client.TestPlaceManager.MyMock;
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
        @Binds @Singleton PlaceManager bindPlaceManager(TestPlaceManager o);
        @Provides @Singleton static MyMock providePlaceManagerWindowMethodsTestUtil() {
            return mock(MyMock.class);
        }
        @Provides static View provideView() { return mock(View.class); }
        @Provides @Singleton static TestScheduler provideTestScheduler() { return new TestScheduler(); }

        @Binds @IntoSet Place bindBasicPlace(BasicPlace o);
        @Binds @IntoSet Place bindRedirectPlace(RedirectPlace o);
        @Binds @IntoSet Place bindRedirectNoHistoryPlace(RedirectNoHistoryPlace o);
    }

    @Singleton @Component(modules = GatekeeperTest.MyModule.class) interface MyComponent {
        MembersInjector<PlaceManagerImplTest> injector();
    }

    static class BasicPresenter extends PresenterChild<View> {
        @Inject BasicPresenter(View v, RootContentSlot root) { super(v, root); }
        @Override public final boolean isVisible() { return super.isVisible(); }
    }

    static class BasicPlace extends Place {
        @Inject BasicPlace(Provider<BasicPresenter> p) { super("basic", asSingle(p)); }
    }

    /** This presenter automatically redirects in prepareFromRequest to PresenterBasic. */
    static class RedirectPresenter extends PresenterChild<View> {
        private final PlaceManager placeManager;
        public PlaceRequest preparedRequest;
        public int prepareFromRequestCalls;
        public int revealInParentCalls;

        @Inject RedirectPresenter(RootContentSlot root, PlaceManager placeManager) {
            super(mock(View.class), root);
            this.placeManager = placeManager;
        }

        @Override public Completable prepareFromRequest(PlaceRequest request) {
            super.prepareFromRequest(request);
            ++prepareFromRequestCalls;
            preparedRequest = request;
            placeManager.revealPlace(PlaceRequest.of("basic").build());
            return Completable.never();
        }

        @Override protected Completable revealInParent() { return Completable.fromAction(() -> ++revealInParentCalls); }
    }

    static class RedirectPlace extends Place {
        @Inject RedirectPlace(Provider<RedirectPresenter> p) { super("redirect", asSingle(p));}
    }

    static class RedirectNoHistoryPresenter extends PresenterChild<View> {
        private final PlaceManager placeManager;
        private static final String TOKEN = "redirectNoHistory";

        @Inject RedirectNoHistoryPresenter(RootContentSlot root, PlaceManager placeManager) {
            super(mock(View.class), root);
            this.placeManager = placeManager;
        }

        @Override public Completable prepareFromRequest(PlaceRequest request) {
            super.prepareFromRequest(request);
            placeManager.revealPlace(PlaceRequest.of("basic").build(), UpdateBrowserUrl.NOOP);
            return Completable.never();
        }

        @Override protected Completable revealInParent() { return Completable.complete(); }
    }

    static class RedirectNoHistoryPlace extends Place {
        @Inject RedirectNoHistoryPlace(Provider<RedirectNoHistoryPresenter> p) {
            super(RedirectNoHistoryPresenter.TOKEN, asSingle(p));
        }
    }

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
    @Inject MyMock gwtWindowMethods;
    @Inject NavigationEventSpy navigationHandler;
    @Inject EventBus eventBus;
    @Inject BasicPresenter presenterBasic;
    @Inject RedirectPresenter presenterRedirect;

    @Test public void placeManagerRevealPlaceStandard() {
        // Given
        eventBus.addHandler(NavigationEvent.TYPE, navigationHandler);

        // When
        placeManager.revealPlace(PlaceRequest.of("basic").with("dummyParam", "dummyValue").build());

        // Then
        PlaceRequest placeRequest = placeManager.getCurrentPlaceRequest();
        assertNotNull(placeRequest);

        assertEquals("basic", placeRequest.getNameToken());
        assertEquals(1, placeRequest.getParameterNames().size());
        assertEquals("dummyValue", placeRequest.getParameter("dummyParam", null));

        verify(presenterBasic).prepareFromRequest(placeRequest);
        verify(presenterBasic).forceReveal();

        verify(gwtWindowMethods).setBrowserHistoryToken(any(String.class), eq(false));

        assertEquals(1, navigationHandler.navCount);
        placeRequest = navigationHandler.lastEvent.getRequest();
        assertEquals("basic", placeRequest.getNameToken());
        assertEquals(1, placeRequest.getParameterNames().size());
        assertEquals("dummyValue", placeRequest.getParameter("dummyParam", null));
    }

    /**
     * DummyPresenterRedirectNoHistory makes a call to revealPlace in prepareFromRequest. This call
     * is deferred but useBrowserUrl must be preserved and the history token must be set only once.
     */
    @Test public void placeManagerRevealPlaceRedirectInPrepareFromRequestNoHistory() {
        // Given
        PlaceRequest placeRequest = PlaceRequest.of(RedirectNoHistoryPresenter.TOKEN).build();

        // When
        placeManager.revealPlace(placeRequest);

        // Then
        verify(gwtWindowMethods, times(1)).setBrowserHistoryToken(any(String.class), eq(false));

        PlaceRequest finalPlaceRequest = placeManager.getCurrentPlaceRequest();
        assertEquals("basic", finalPlaceRequest.getNameToken());
    }

    @Test public void placeManagerRevealPlaceRedirectInPrepareFromRequest() {
        // Given
        PlaceRequest placeRequest = PlaceRequest.of("dummyNameTokenRedirect").with("dummyParam", "dummyValue").build();

        // When
        placeManager.revealPlace(placeRequest);

        // Then
        PlaceRequest finalPlaceRequest = placeManager.getCurrentPlaceRequest();
        assertNotNull(finalPlaceRequest);

        assertEquals("basic", finalPlaceRequest.getNameToken());
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
