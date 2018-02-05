package com.intendia.reactivity.client;

import static org.mockito.Mockito.mock;

import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Set;
import javax.inject.Inject;

/**
 * This place manager overrides all the methods that use GWT-dependent classes and can be used for testing without
 * having to rely on a {@code GWTTestCase}.
 */
public class PlaceManagerTestUtil extends PlaceManager {
    private final PlaceManagerWindowMethodsTestUtil gwtWindowMethods;

    @Inject PlaceManagerTestUtil(EventBus eventBus, TokenFormatter tokenFormatter,
            PlaceManagerWindowMethodsTestUtil gwtWindowMethods, Set<Proxy<? extends PresenterChild<?>>> places) {
        super(eventBus, tokenFormatter, mock(Historian.class), places, "defaultPlace", "defaultPlace", "defaultPlace");
        this.gwtWindowMethods = gwtWindowMethods;
    }

    @Override String getBrowserHistoryToken() { return gwtWindowMethods.getBrowserHistoryToken(); }
    @Override public void revealCurrentPlace() { gwtWindowMethods.revealCurrentPlace(); }
    @Override void setBrowserHistoryToken(String historyToken, boolean issueEvent) {
        gwtWindowMethods.setBrowserHistoryToken(historyToken, issueEvent);
    }
}
