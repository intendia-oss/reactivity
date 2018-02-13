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

    public String getNameToken() { return nameToken; }
    public boolean matchesRequest(PlaceRequest request) { return request.matchesNameToken(nameToken); }
    public boolean canReveal(PlaceRequest request) { return gatekeeper.canReveal(request); }
    public Single<? extends PresenterChild<?>> getPresenter() { return presenter; }

    @Override public final boolean equals(Object o) { return o instanceof Place && equals((Place) o); }
    private boolean equals(Place o) { return nameToken.equals(o.nameToken) || nameToken.equals(o.nameToken); }
    @Override public final int hashCode() { return 17 * nameToken.hashCode(); }
    @Override public final String toString() { return nameToken; }
}
