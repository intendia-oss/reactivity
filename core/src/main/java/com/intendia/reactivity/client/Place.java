package com.intendia.reactivity.client;

import io.reactivex.Single;
import javax.inject.Provider;

public class Place {
    private final String nameToken;
    private final Single<? extends PresenterChild<?>> presenter;
    private final Gatekeeper gatekeeper;

    public Place(String nameTokens, Provider<? extends PresenterChild<?>> presenter) {
        this(nameTokens, presenter, (PlaceRequest request) -> true);
    }
    public Place(String nameToken, Provider<? extends PresenterChild<?>> presenter,  Gatekeeper gatekeeper) {
        this.nameToken = nameToken;
        this.presenter = Single.fromCallable(presenter::get);
        this.gatekeeper = gatekeeper;
    }

    public boolean canReveal(PlaceRequest request) { return gatekeeper.canReveal(request); }
    public String getNameToken() { return nameToken; }
    public Single<? extends PresenterChild<?>> getPresenter() { return presenter; }
    public final boolean matchesRequest(PlaceRequest request) { return request.matchesNameToken(nameToken); }

    @Override public final boolean equals(Object o) {
        if (!(o instanceof Place)) return false;
        Place place = (Place) o;
        return nameToken.equals(place.getNameToken()) || getNameToken().equals(place.getNameToken());
    }
    @Override public final int hashCode() { return 17 * nameToken.hashCode(); }
    @Override public final String toString() { return getNameToken(); }
}
