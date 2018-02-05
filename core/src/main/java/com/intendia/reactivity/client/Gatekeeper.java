package com.intendia.reactivity.client;

@FunctionalInterface
public interface Gatekeeper {
    boolean canReveal(PlaceRequest request);

    @FunctionalInterface interface Factory {
        Gatekeeper create(String... params);
    }
}
