package com.intendia.reactivity.client;

import static com.intendia.reactivity.client.RevealableComponent.PREPARE_FROM_REQUEST;
import static io.reactivex.Completable.defer;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.intendia.reactivity.client.PlaceNavigator.PlaceNavigation;
import com.intendia.reactivity.client.TokenFormatter.TokenFormatException;
import io.reactivex.Completable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

public class PlaceManager {

    private final EventBus eventBus;
    private final TokenFormatter tokenFormatter;
    private final Historian historian;
    private final Set<Place> places;
    private final PlaceNavigator placeNavigator;

    private String currentHistoryToken = "";
    private boolean internalError;
    private String onLeaveQuestion;
    private Command deferredNavigation;

    private HandlerRegistration windowClosingHandlerRegistration;
    private boolean locked;

    private PlaceRequest place = new PlaceRequest.Builder().build();

    @Inject protected PlaceManager(
            EventBus bus,
            TokenFormatter tokenFormatter,
            Historian historian,
            Set<Place> places,
            PlaceNavigator placeNavigator) {
        this.eventBus = bus;
        this.tokenFormatter = tokenFormatter;
        this.historian = historian;
        this.places = places;
        this.historian.addValueChangeHandler(event -> handleTokenChange(event.getValue()));
        this.placeNavigator = placeNavigator;
    }

    /**
     * If a confirmation question is set (see {@link #setOnLeaveConfirmation(String)}), this asks the user if he wants
     * to leave the current page.
     *
     * @return true if the user accepts to leave. false if he refuses.
     */
    private boolean confirmLeaveState() {
        if (onLeaveQuestion == null) {
            return true;
        }
        boolean confirmed = Window.confirm(onLeaveQuestion);
        if (confirmed) {
            // User has confirmed, don't ask any more question.
            setOnLeaveConfirmation(null);
        } else {
            fireEvent(new NavigationRefusedEvent());
            setBrowserHistoryToken(currentHistoryToken, false);
        }
        return confirmed;
    }

    /** Do not call this method directly, instead call {@link #revealPlace(PlaceRequest)} or a related method. */
    protected void doRevealPlace(PlaceRequest request) {
        Optional<Place> matches = places.stream()
                .filter(place -> place.matchesRequest(request))
                .findFirst();
        if (!matches.isPresent()) {
            unlock();
            error(tokenFormatter.toPlaceToken(request));
        } else if (!requireNonNull(matches.get()).canReveal(request)) {
            unlock();
            illegalAccess(tokenFormatter.toPlaceToken(request));
        } else {
            matches.get().getPresenter().flatMapCompletable(p -> {
                return p.req(PREPARE_FROM_REQUEST).apply(request).andThen(defer(() -> {
                    // User might manually update place request in prepareFrom request, so always read it again
                    fireEvent(new NavigationEvent(getCurrentPlaceRequest()));

                    // Reveal only if there are no pending navigation requests
                    if (hasPendingNavigation()) return Completable.complete();

                    if (!p.isVisible()) return p.revealInParent(); // This will trigger a reset in due time
                    else p.performReset(); // Otherwise, we have to do the reset ourselves
                    return Completable.complete();
                }));
            }).doOnTerminate(this::unlock).subscribe(); //XXX eliminate all subscribe calls!
        }
    }

    /**
     * Called whenever an error occurred that requires the error page to be shown to the user. This method will detect
     * infinite reveal loops and throw an {@link RuntimeException} in that case.
     */
    private void error(String invalidHistoryToken) {
        startError();
        revealErrorPlace(invalidHistoryToken);
        stopError();
    }

    private void fireEvent(GwtEvent<?> event) { getEventBus().fireEventFromSource(event, this); }

    public PlaceRequest getCurrentPlaceRequest() { return place; }

    public EventBus getEventBus() { return eventBus; }

    /**
     * Checks that the place manager is not locked and that the user allows the application to navigate (see {@link
     * #confirmLeaveState()}. If the application is allowed to navigate, this method locks navigation.
     *
     * @return true if the place manager can get the lock false otherwise.
     */
    private boolean getLock() {
        if (locked) return false;
        if (!confirmLeaveState()) return false;
        lock();
        return true;
    }

    public boolean hasPendingNavigation() { return deferredNavigation != null; }

    /**
     * Called whenever the user tries to access an page to which he doesn't have access, and we need to reveal the
     * user-defined unauthorized place. This method will detect infinite reveal loops and throw an {@link
     * RuntimeException} in that case.
     *
     * @param historyToken The history token that was not recognised.
     */
    private void illegalAccess(String historyToken) {
        startError();
        revealUnauthorizedPlace(historyToken);
        stopError();
    }

    private void lock() {
        if (!locked) {
            locked = true;
            fireEvent(new LockInteractionEvent(true));
        }
    }

    public void navigateBack() {
        History.back();
    }

    private void handleTokenChange(String historyToken) {
        if (locked) {
            deferredNavigation = () -> handleTokenChange(historyToken);
            return;
        }
        if (!getLock()) {
            return;
        }
        try {
            if (historyToken.trim().isEmpty()) {
                unlock();
                revealDefaultPlace();
            } else {
                place = tokenFormatter.toPlaceRequest(historyToken);
                doRevealPlace(getCurrentPlaceRequest());
            }
        } catch (TokenFormatException e) {
            unlock();
            error(historyToken);
            fireEvent(new NavigationEvent(null));
        }
    }

    public void revealCurrentPlace() { handleTokenChange(historian.getToken()); }

    public final void revealDefaultPlace() { revealPlaceFrom(placeNavigator.defaultNavigation()); }

    public void revealErrorPlace(String invalidHistoryToken) {
        revealPlaceFrom(placeNavigator.errorPlaceNavigation(invalidHistoryToken));
    }

    public void revealUnauthorizedPlace(String unauthorizedHistoryToken) {
        revealPlaceFrom(placeNavigator.unauthorizedPlaceNavigation(unauthorizedHistoryToken));
    }

    public void revealPlace(PlaceRequest request) { revealPlace(request, HistoryUpdate.ADD); }

    public void revealPlace(PlaceRequest request, HistoryUpdate mode) {
        requireNonNull(request, "request required");
        if (locked) {
            deferredNavigation = () -> revealPlace(request, mode);
            return;
        }
        if (!getLock()) {
            return;
        }
        place = request;
        updateHistory(request, mode);
        doRevealPlace(request);
    }

    private void revealPlaceFrom(PlaceNavigation placeNavigation) {
        revealPlace(placeNavigation.placeRequest, placeNavigation.update);
    }

    /**
     * This method saves the history token, making it possible to correctly restore the browser's URL if the user
     * refuses to navigate.
     */
    private void saveHistoryToken(String historyToken) {
        currentHistoryToken = historyToken;
    }

    @VisibleForTesting void setBrowserHistoryToken(String historyToken, boolean issueEvent) {
        historian.newItem(historyToken, issueEvent);
    }

    public void setOnLeaveConfirmation(String question) {
        if (question == null && onLeaveQuestion == null) {
            return;
        }
        if (question != null && onLeaveQuestion == null) {
            windowClosingHandlerRegistration = Window.addWindowClosingHandler(event -> {
                // The current implementation has a few bugs described below. However these are browser
                // bugs, and the workarounds we've experimented with gave worst results than the bug itself.
                //
                // Here are the current behaviours of different browsers after cancelling navigation
                // * Chrome
                //    - URL bar shows new website (FAIL)
                //    - Bookmarking uses the title of the webapp, but url of new website (FAIL)
                //    - Navigating away and then back goes back to the correct webapp page (WORKS)
                // * Firefox
                //    - URL bar shows new website (FAIL)
                //    - Bookmarking uses the title of the webapp, and url of webapp (WORKS)
                //    - Navigating away and then back goes back to the correct webapp page (WORKS)
                // * IE
                //    - Untested
                //
                // Options are to report that upstream in the browsers or to go back to our workarounds in a
                // browser-dependent fashion using deferred binding. The workarounds we've experimented with
                // consisted of adding a deferred command that used Window.Location.replace to reset the URL
                // to the current page. However, this caused infinite loops in some browsers.
                //
                // See this issue:
                //   http://code.google.com/p/gwt-platform/issues/detail?id=315

                event.setMessage(onLeaveQuestion);
            });
        }
        if (question == null) {
            windowClosingHandlerRegistration.removeHandler();
        }
        onLeaveQuestion = question;
    }

    /** Start revealing an error or unauthorized page and will throw an exception if an infinite loop is detected. */
    private void startError() {
        if (internalError) throw new RuntimeException("Encountered repeated errors resulting in an infinite loop. "
                + "Make sure all users have access to the pages revealed by revealErrorPlace and "
                + "revealUnauthorizedPlace. (Note that the default implementations call revealDefaultPlace)");
        internalError = true;
    }

    /** Indicates that an error page has successfully been revealed. Makes it possible to detect infinite loops. */
    private void stopError() {
        internalError = false;
    }

    public void unlock() {
        if (locked) {
            locked = false;
            fireEvent(new LockInteractionEvent(false));
            if (hasPendingNavigation()) {
                Command navigation = deferredNavigation;
                deferredNavigation = null;
                navigation.execute();
            }
        }
    }

    public enum HistoryUpdate {
        /** Does not update the browser URL. */ NOOP(false),
        /** Updates the browser URL adding a new history item. */ ADD(true),
        /** Updates the browser URL replacing the last history item. */ REPLACE(true);
        private final boolean change;
        HistoryUpdate(boolean change) { this.change = change;}
    }

    public void updateHistory(PlaceRequest request, HistoryUpdate mode) {
        try {
            // Make sure the request match, externally can only be used to change parameters
            assert request.matchesNameToken(place) : "name token mismatch";
            place = request; // update it, parameter can be different (this is the whole point)
            if (mode.change) {
                String historyToken = tokenFormatter.toPlaceToken(place);
                if (!Objects.equals(historyToken, historian.getToken())) switch (mode) {
                    case ADD: historian.newItem(historyToken, false); break;
                    case REPLACE: historian.replaceItem(historyToken, false); break;
                }
                saveHistoryToken(historyToken);
            }
        } catch (TokenFormatException ignore) {}
    }

    /** Builds a string corresponding to the history token to reveal the specified {@link PlaceRequest}. */
    public String buildHistoryToken(PlaceRequest request) { return tokenFormatter.toPlaceToken(request);}

    public static class NavigationEvent extends GwtEvent<NavigationEvent.NavigationHandler> {
        public static final Type<NavigationHandler> TYPE = new Type<>();
        public static Type<NavigationHandler> getType() { return TYPE; }
        private PlaceRequest request;
        public NavigationEvent(PlaceRequest request) { this.request = request; }
        @Override public Type<NavigationHandler> getAssociatedType() { return TYPE; }
        public PlaceRequest getRequest() { return request; }
        @Override protected void dispatch(NavigationHandler handler) { handler.onNavigation(this); }
        public interface NavigationHandler extends EventHandler {
            void onNavigation(NavigationEvent navigationEvent);
        }
    }

    public static class NavigationRefusedEvent extends GwtEvent<NavigationRefusedEvent.NavigationRefusedHandler> {
        public static final Type<NavigationRefusedHandler> TYPE = new Type<>();
        public static Type<NavigationRefusedHandler> getType() { return TYPE; }
        @Override public Type<NavigationRefusedHandler> getAssociatedType() { return TYPE; }
        @Override protected void dispatch(NavigationRefusedHandler handler) { handler.onNavigationRefused(this); }
        public interface NavigationRefusedHandler extends EventHandler {
            void onNavigationRefused(NavigationRefusedEvent navigationRefusedEvent);
        }
    }

    public static class LockInteractionEvent extends GwtEvent<LockInteractionEvent.LockInteractionHandler> {
        public static final Type<LockInteractionHandler> TYPE = new Type<>();
        private boolean lock;
        public LockInteractionEvent(boolean lock) { this.lock = lock; }
        public boolean shouldLock() { return lock; }
        @Override public Type<LockInteractionHandler> getAssociatedType() { return TYPE; }
        @Override protected void dispatch(LockInteractionHandler handler) { handler.onLockInteraction(this); }
        public interface LockInteractionHandler extends EventHandler {
            void onLockInteraction(LockInteractionEvent lockInteractionEvent);
        }
    }

    public interface Historian {
        String getToken();
        void newItem(String token, boolean issueEvent);
        void replaceItem(String token, boolean issueEvent);
        HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler);
    }

    public static class DefaultHistorian implements Historian {
        @Override public String getToken() { return History.getToken();}
        @Override public void newItem(String token, boolean issueEvent) { History.newItem(token, issueEvent);}
        @Override public void replaceItem(String token, boolean issueEvent) { History.replaceItem(token, issueEvent);}
        @Override public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
            return History.addValueChangeHandler(handler);
        }
    }
}
