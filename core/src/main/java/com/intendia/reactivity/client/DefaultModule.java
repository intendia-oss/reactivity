package com.intendia.reactivity.client;

import com.google.gwt.place.shared.PlaceHistoryHandler.DefaultHistorian;
import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class DefaultModule {
    private final String defaultPlaceToken, errorPlaceToken, unauthorizedPlaceToken;
    public DefaultModule(String defaultPlaceToken, String errorPlaceToken, String unauthorizedPlaceToken) {
        this.defaultPlaceToken = defaultPlaceToken;
        this.errorPlaceToken = errorPlaceToken;
        this.unauthorizedPlaceToken = unauthorizedPlaceToken;
    }
    @Provides @Singleton EventBus provideEventBus() { return new SimpleEventBus(); }
    @Provides @Singleton Historian provideHistorian() { return new DefaultHistorian(); }
    @Provides @Singleton TokenFormatter provideTokenFormatter(ParameterTokenFormatter o) { return o; }
    @Provides @PlaceManager.DefaultPlace String provideDefaultPlace() { return defaultPlaceToken; }
    @Provides @PlaceManager.ErrorPlace String provideErrorPlace() { return errorPlaceToken; }
    @Provides @PlaceManager.UnauthorizedPlace String provideUnauthorizedPlace() { return unauthorizedPlaceToken; }
}
