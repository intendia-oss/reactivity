package com.intendia.reactivity.client;

/** GWT window and history related methods that can be mocked. */
public interface PlaceManagerWindowMethodsTestUtil {
    void registerTowardsHistory();

    String getBrowserHistoryToken();

    String getCurrentHref();

    void revealCurrentPlace();

    void setBrowserHistoryToken(String historyToken, boolean issueEvent);
}
