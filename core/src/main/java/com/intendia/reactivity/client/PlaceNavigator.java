package com.intendia.reactivity.client;

import static com.intendia.reactivity.client.PlaceManager.HistoryUpdate.ADD;
import static com.intendia.reactivity.client.PlaceManager.HistoryUpdate.NOOP;
import static com.intendia.reactivity.client.PlaceManager.HistoryUpdate.REPLACE;

import com.intendia.reactivity.client.PlaceManager.HistoryUpdate;

/** Strategy for selecting places for common navigation scenarios */
public interface PlaceNavigator {

    /**
     * Builds a navigation to the default place. This is invoked when the history token is empty and no places
     * handled it. Implementations should build a {@link PlaceNavigation} with a {@link PlaceRequest}
     * corresponding to their default presenter. Consider returning {@link HistoryUpdate#NOOP} as update value,
     * Otherwise a new token will be inserted in the browser's history and hitting the browser's
     * <em>back</em> button will not take the user out of the application.
     *
     * <p><b>Important!</b> Make sure you build a valid {@link PlaceRequest} and that the user has access
     * to it, otherwise you might create an infinite loop.</p>
     */
    PlaceNavigation defaultNavigation();
    PlaceNavigation errorNavigation(Throwable throwable);

    class PlaceNavigation {
        public final PlaceRequest placeRequest;
        public final HistoryUpdate update;
        public static PlaceNavigation of(PlaceRequest placeRequest, HistoryUpdate update) {
            return new PlaceNavigation(placeRequest, update);
        }
        public static PlaceNavigation noop(PlaceRequest placeRequest) { return of(placeRequest, NOOP); }
        public static PlaceNavigation add(PlaceRequest placeRequest) { return of(placeRequest, ADD); }
        public static PlaceNavigation replace(PlaceRequest placeRequest) { return of(placeRequest, REPLACE); }

        PlaceNavigation(PlaceRequest placeRequest, HistoryUpdate update) {
            this.placeRequest = placeRequest;
            this.update = update;
        }
    }
}
