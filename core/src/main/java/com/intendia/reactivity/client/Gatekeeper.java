package com.intendia.reactivity.client;

@FunctionalInterface
public interface Gatekeeper {
    Gatekeeper PUBLIC = (PlaceRequest request) -> {
    };

    default boolean canReveal(PlaceRequest request) {
        try {
            checkReveal(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void checkReveal(PlaceRequest request) throws PlaceException;
}
