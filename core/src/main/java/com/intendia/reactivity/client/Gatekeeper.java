package com.intendia.reactivity.client;

@FunctionalInterface
public interface Gatekeeper {
    Gatekeeper PUBLIC = (PlaceRequest request) -> true;

    boolean canReveal(PlaceRequest request);
}
