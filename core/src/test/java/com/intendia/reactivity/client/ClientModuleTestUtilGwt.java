package com.intendia.reactivity.client;

import static org.mockito.Mockito.mock;

import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
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
    @Provides @Singleton static PlaceManager providePlaceManager(EventBus bus, Set<Place> places) {
        return new PlaceManager(bus, mock(ParameterTokenFormatter.class),
                mock(PlaceHistoryHandler.Historian.class), places, "home", "home", "home") {};
    }
}
