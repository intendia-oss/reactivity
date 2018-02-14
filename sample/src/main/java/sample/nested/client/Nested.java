package sample.nested.client;

import com.google.gwt.core.client.EntryPoint;
import com.intendia.reactivity.client.PlaceManager;
import dagger.Component;
import javax.inject.Singleton;
import sample.nested.client.resources.Resources;

public class Nested implements EntryPoint {
    @Override public void onModuleLoad() {
        Resources.inject();
        DaggerNested_ClientComponent.create().placeManager().revealCurrentPlace();
    }

    @Singleton @Component(modules = ClientModule.class) interface ClientComponent {
        PlaceManager placeManager();
    }
}
