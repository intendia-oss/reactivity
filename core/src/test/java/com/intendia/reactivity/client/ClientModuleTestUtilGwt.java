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
    @Binds @IntoSet abstract Proxy<? extends PresenterChild<?>> bindMainPresenterTestUtilGwt(MainPresenterTestUtilGwt.MyProxy o);
    @Binds @IntoSet abstract Proxy<? extends PresenterChild<?>> bindAdminPresenterTestUtilGwt(AdminPresenterTestUtilGwt.MyProxy o);
    @Provides @Named("notice") static String provideNotice() { return "Hello"; }

    // default module
    @Provides @Singleton static EventBus provideEventBus() { return new SimpleEventBus(); }
    @Provides @Singleton static PlaceManager providePlaceManager(EventBus bus, Set<Proxy<? extends PresenterChild<?>>> places) {
        return new PlaceManager(bus, mock(ParameterTokenFormatter.class),
                mock(PlaceHistoryHandler.Historian.class), places, "home", "home", "home") {};
    }
}
