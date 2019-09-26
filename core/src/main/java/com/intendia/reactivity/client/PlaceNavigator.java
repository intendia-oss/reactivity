package com.intendia.reactivity.client;

import static com.intendia.reactivity.client.PlaceManager.HistoryUpdate.ADD;
import static com.intendia.reactivity.client.PlaceManager.HistoryUpdate.NOOP;
import static com.intendia.reactivity.client.PlaceManager.HistoryUpdate.REPLACE;
import static com.intendia.reactivity.client.PlaceNavigator.PlaceNavigation.noop;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.intendia.reactivity.client.PlaceManager.HistoryUpdate;
import java.lang.annotation.Retention;
import javax.inject.Inject;
import javax.inject.Qualifier;

/** Strategy for selecting places for common navigation scenarios */
public interface PlaceNavigator {

    /**
     * Builds a navigation to the default place. This is invoked when the history token is empty and no places
     * handled it. Implementations should build a {@link PlaceNavigation} with a {@link PlaceRequest}
     * corresponding to their default presenter. Consider returning {@link HistoryUpdate#NOOP} as update value,
     * Otherwise a new token will be inserted in the browser's history and hitting the browser's
     * <em>back</em> button will not take the user out of the application.
     * <p/>
     * <b>Important!</b> Make sure you build a valid {@link PlaceRequest} and that the user has access
     * to it, otherwise you might create an infinite loop.
     */
    PlaceNavigation defaultNavigation();
    PlaceNavigation errorPlaceNavigation(String token);
    PlaceNavigation unauthorizedPlaceNavigation(String token);

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

    /** Simple implementation of PlaceSelector with fixed place request selected at build time */
    class DefaultPlaceNavigator implements PlaceNavigator {

        public @Qualifier @Retention(RUNTIME) @interface DefaultPlace {}

        public @Qualifier @Retention(RUNTIME) @interface ErrorPlace {}

        public @Qualifier @Retention(RUNTIME) @interface UnauthorizedPlace {}

        private final PlaceRequest defaultPlaceRequest;
        private final PlaceRequest errorPlaceRequest;
        private final PlaceRequest unauthorizedPlaceRequest;

        @Inject DefaultPlaceNavigator(
                @DefaultPlace PlaceRequest defaultPlaceRequest,
                @ErrorPlace PlaceRequest errorPlaceRequest,
                @UnauthorizedPlace PlaceRequest unauthorizedPlaceRequest) {
            this.defaultPlaceRequest = defaultPlaceRequest;
            this.errorPlaceRequest = errorPlaceRequest;
            this.unauthorizedPlaceRequest = unauthorizedPlaceRequest;
        }

        @Override public PlaceNavigation defaultNavigation() { return noop(defaultPlaceRequest); }
        @Override public PlaceNavigation errorPlaceNavigation(String token) { return noop(errorPlaceRequest); }
        @Override public PlaceNavigation unauthorizedPlaceNavigation(String token) {
            return noop(unauthorizedPlaceRequest);
        }
    }
}
