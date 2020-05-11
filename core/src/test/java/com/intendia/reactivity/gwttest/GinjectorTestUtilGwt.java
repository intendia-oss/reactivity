package com.intendia.reactivity.gwttest;

import com.intendia.reactivity.client.PlaceManager;
import dagger.Component;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton @Component(modules = ClientModuleTestUtilGwt.class)
public interface GinjectorTestUtilGwt {
    Provider<MainPresenterTestUtilGwt> getMainPresenter();

    Provider<AdminPresenterTestUtilGwt> getAdminPresenter();

    Provider<PopupPresenterTestUtilGwt> getPopupPresenter();

    PlaceManager getPlaceManager();
}
