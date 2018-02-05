package com.intendia.reactivity.client;

public class Place {
    private final String nameToken;
    private final Gatekeeper gatekeeper;

    public Place(String nameTokens) {
        this(nameTokens, (PlaceRequest request) -> true);
    }
    public Place(String nameToken, Gatekeeper gatekeeper) {
        this.nameToken = nameToken;
        this.gatekeeper = gatekeeper;
    }

    public boolean canReveal(PlaceRequest request) { return gatekeeper.canReveal(request); }
    public String getNameToken() { return nameToken; }
    public final boolean matchesRequest(PlaceRequest request) { return request.matchesNameToken(nameToken); }

    @Override public final boolean equals(Object o) {
        if (!(o instanceof Place)) return false;
        Place place = (Place) o;
        return nameToken.equals(place.getNameToken()) || getNameToken().equals(place.getNameToken());
    }
    @Override public final int hashCode() { return 17 * nameToken.hashCode(); }
    @Override public final String toString() { return getNameToken(); }
}
