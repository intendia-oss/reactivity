package sample.nested.client;

import com.intendia.reactivity.client.DefaultModule;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PlaceManager.DefaultPlace;
import com.intendia.reactivity.client.PlaceManager.ErrorPlace;
import com.intendia.reactivity.client.PlaceManager.UnauthorizedPlace;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import sample.nested.client.application.AboutUsPresenter;
import sample.nested.client.application.ContactPresenter;
import sample.nested.client.application.HomePresenter;

@Module(includes = DefaultModule.class)
public interface ClientModule {
    @Provides @DefaultPlace static String provideDefaultPlace() { return NameTokens.homePage; }
    @Provides @ErrorPlace static String provideErrorPlace() { return NameTokens.homePage; }
    @Provides @UnauthorizedPlace static String provideUnauthorizedPlace() { return NameTokens.homePage; }

    @Binds @IntoSet Place bindHomeProxy(HomePresenter.MyPlace o);
    @Binds @IntoSet Place bindAboutUsProxy(AboutUsPresenter.MyPlace o);
    @Binds @IntoSet Place bindContactProxy(ContactPresenter.MyPlace o);
}
