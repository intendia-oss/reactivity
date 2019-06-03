package sample.nested.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.intendia.reactivity.client.ParameterTokenFormatter;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PlaceManager;
import com.intendia.reactivity.client.PlaceManager.DefaultHistorian;
import com.intendia.reactivity.client.PlaceManager.DefaultPlace;
import com.intendia.reactivity.client.PlaceManager.ErrorPlace;
import com.intendia.reactivity.client.PlaceManager.Historian;
import com.intendia.reactivity.client.PlaceManager.UnauthorizedPlace;
import com.intendia.reactivity.client.TokenFormatter;
import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.multibindings.IntoSet;
import io.reactivex.Single;
import java.util.function.Supplier;
import javax.inject.Singleton;
import sample.nested.client.application.AboutUsPresenter;
import sample.nested.client.application.ContactPresenter;
import sample.nested.client.application.EmptyPresenter;
import sample.nested.client.application.HomePresenter;
import sample.nested.client.resources.Resources;

public class SampleEntryPoint implements EntryPoint {
    @Override public void onModuleLoad() {
        Resources.inject();
        DaggerSampleEntryPoint_ClientComponent.create().router().revealCurrentPlace();
    }

    @Component(modules = ClientModule.class) @Singleton interface ClientComponent {
        PlaceManager router();
    }

    @Module(includes = DefaultModule.class, subcomponents = ClientModule.Presenters.class)
    public interface ClientModule {
        // fallback places; required by PlaceManager to handle default, error and unauthorized cases
        @Binds @DefaultPlace Place provideDefaultPlace(HomePresenter.MyPlace o);
        @Binds @ErrorPlace Place provideErrorPlace(EmptyPresenter.MyPlace o);
        @Binds @UnauthorizedPlace Place provideUnauthorizedPlace(EmptyPresenter.MyPlace o);

        // included in initial bundle
        @Binds @IntoSet Place bindEmptyPlace(EmptyPresenter.MyPlace o);

        // loaded using code splitting when any of this presenters gets visited
        @Binds @IntoSet Place bindHomePlace(HomePresenter.MyPlace o);
        @Binds @IntoSet Place bindAboutUsPlace(AboutUsPresenter.MyPlace o);
        @Binds @IntoSet Place bindContactPlace(ContactPresenter.MyPlace o);

        // we group and hide presenters to encourage code-splitting
        @Subcomponent interface Presenters {
            HomePresenter home();
            AboutUsPresenter aboutUs();
            ContactPresenter contact();
            @Subcomponent.Builder interface Builder extends Supplier<Presenters> {}
        }

        // and we use a async lazy Presenters component to access to all the presenter in the split
        @Provides @Singleton static Single<Presenters> presenters(Presenters.Builder builder) {
            return Single.create(s -> GWT.runAsync(new RunAsyncCallback() {
                @Override public void onFailure(Throwable reason) { s.onError(reason); }
                @Override public void onSuccess() { s.onSuccess(builder.get()); }
            }));
        }
    }

    @Module
    public interface DefaultModule {
        @Provides @Singleton static EventBus provideEventBus() { return new SimpleEventBus(); }
        @Provides @Singleton static Historian provideHistorian() { return new DefaultHistorian(); }
        @Binds @Singleton TokenFormatter provideTokenFormatter(ParameterTokenFormatter o);
    }
}
