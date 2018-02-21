package sample.nested.client;

import static sample.nested.client.NameTokens.emptyPage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.intendia.reactivity.client.DefaultModule;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PlaceManager;
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
        DaggerSampleEntryPoint_ClientComponent.builder()
                .defaultModule(new DefaultModule(emptyPage, emptyPage, emptyPage))
                .build().placeManager().revealCurrentPlace();
    }

    @Component(modules = ClientModule.class) @Singleton interface ClientComponent {
        PlaceManager placeManager();
    }

    @Module(includes = DefaultModule.class, subcomponents = ClientModule.Presenters.class)
    public interface ClientModule {
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
}
