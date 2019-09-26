package com.intendia.reactivity.client;

import io.reactivex.Single;
import javax.inject.Provider;

public class Place {
    private final String nameToken;
    private final Single<? extends RevealableComponent> presenter;
    private final Gatekeeper gatekeeper;

    public Place(String nameTokens, Single<? extends RevealableComponent> presenter) {
        this(nameTokens, presenter, Gatekeeper.PUBLIC);
    }

    public Place(String nameToken, Single<? extends RevealableComponent> presenter, Gatekeeper gatekeeper) {
        this.nameToken = nameToken;
        this.presenter = presenter;
        this.gatekeeper = gatekeeper;
    }

    public String getNameToken() { return nameToken; }
    public boolean matchesRequest(PlaceRequest request) { return request.matchesNameToken(getNameToken()); }
    public void checkReveal(PlaceRequest request) { gatekeeper.checkReveal(request); }
    public Single<? extends RevealableComponent> getPresenter() { return presenter; }

    @Override public final boolean equals(Object o) { return o instanceof Place && equals((Place) o); }
    private boolean equals(Place o) { return nameToken.equals(o.nameToken); }
    @Override public final int hashCode() { return 17 * nameToken.hashCode(); }
    @Override public final String toString() { return getNameToken(); }

    public static <T> Single<T> asSingle(Provider<T> presenter) {
        return Single.fromCallable(presenter::get);
    }
}
