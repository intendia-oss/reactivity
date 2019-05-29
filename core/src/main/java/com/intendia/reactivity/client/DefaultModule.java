package com.intendia.reactivity.client;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.intendia.reactivity.client.PlaceManager.DefaultHistorian;
import com.intendia.reactivity.client.PlaceManager.Historian;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public interface DefaultModule {
    @Provides @Singleton static EventBus provideEventBus() { return new SimpleEventBus(); }
    @Provides @Singleton static Historian provideHistorian() { return new DefaultHistorian(); }
    @Binds @Singleton TokenFormatter provideTokenFormatter(ParameterTokenFormatter o);
}
