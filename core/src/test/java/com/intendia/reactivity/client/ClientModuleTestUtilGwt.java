package com.intendia.reactivity.client;

import static org.mockito.Mockito.mock;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.intendia.reactivity.client.PlaceManager.Historian;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public abstract class ClientModuleTestUtilGwt {
    // install(new DefaultModule.Builder().defaultPlace("home").errorPlace("home").unauthorizedPlace("home").build());
    @Binds @IntoSet abstract Place bindMainPresenterTestUtilGwt(MainPresenterTestUtilGwt.MyPlace o);
    @Binds @IntoSet abstract Place bindAdminPresenterTestUtilGwt(AdminPresenterTestUtilGwt.MyPlace o);
    @Provides @Named("notice") static String provideNotice() { return "Hello"; }

    // default module
    @Provides @Singleton static EventBus provideEventBus() { return new SimpleEventBus(); }
    @Provides @Singleton static PlaceNavigator providePlaceNavigator(MainPresenterTestUtilGwt.MyPlace p) {
        PlaceRequest request = PlaceRequest.of(p.getNameToken()).build();
        return new PlaceNavigator.DefaultPlaceNavigator(request, request, request);
    }
    @Provides @Singleton static PlaceManager providePlaceManager(EventBus bus, Set<Place> places,
            PlaceNavigator placeNavigator) {
        return new PlaceManager(bus, mock(ParameterTokenFormatter.class), mock(Historian.class), places,
                placeNavigator) {};
    }
}
