package com.intendia.reactivity.client;

import com.google.gwt.place.shared.PlaceHistoryHandler.DefaultHistorian;
import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public abstract class DefaultModule {
    @Provides @Singleton static EventBus provideEventBus() { return new SimpleEventBus(); }
    @Provides @Singleton static Historian bindHistorian() { return new DefaultHistorian(); }
    @Binds @Singleton abstract TokenFormatter bindTokenFormatter(ParameterTokenFormatter o);
}
