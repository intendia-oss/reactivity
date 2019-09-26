package com.intendia.reactivity.client;

import static org.mockito.Mockito.mock;

import com.google.web.bindery.event.shared.EventBus;
import java.util.Set;
import javax.inject.Inject;

/**
 * This place manager overrides all the methods that use GWT-dependent classes and can be used for testing without
 * having to rely on a {@code GWTTestCase}.
 */
public class TestPlaceManager extends PlaceManager {
    private final MyMock mock;

    @Inject TestPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter,
            MyMock mock, Set<Place> places, PlaceNavigator placeNavigator) {
        super(eventBus, tokenFormatter, mock(Historian.class), places, placeNavigator);
        this.mock = mock;
    }

    @Override public void revealCurrentPlace() { mock.revealCurrentPlace(); }
    @Override void setBrowserHistoryToken(String historyToken, boolean issueEvent) {
        mock.setBrowserHistoryToken(historyToken, issueEvent);
    }

    /** GWT window and history related methods that can be mocked. */
    public interface MyMock {
        void revealCurrentPlace();
        void setBrowserHistoryToken(String historyToken, boolean issueEvent);
    }
}
