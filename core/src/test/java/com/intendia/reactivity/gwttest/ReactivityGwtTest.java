package com.intendia.reactivity.gwttest;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.junit.client.GWTTestCase;
import com.intendia.reactivity.client.PlaceRequest;
import junit.framework.TestCase;

public class ReactivityGwtTest extends GWTTestCase {
    GinjectorTestUtilGwt ginjector;
    MainPresenterTestUtilGwt presenter;

    @Override public String getModuleName() { return "com.intendia.reactivity.gwttest.ReactivityGwtTest"; }

    @Override protected void gwtSetUp() throws Exception {
        super.gwtSetUp();

        ginjector = DaggerGinjectorTestUtilGwt.create();
        presenter = ginjector.getMainPresenter().get();
    }

    /** Verifies that the ginjector is created only once. */
    public void test_place_manger_is_singleton() {
        ginjector.getPlaceManager().revealCurrentPlace();
        assertSame(ginjector.getPlaceManager(), ginjector.getPlaceManager());
    }

    public void testPopupViewCloseHandlerNotCalledWhenShown() {
        delayTestFinish(1000);
        runTest(() -> {
            PopupPresenterTestUtilGwt popupPresenter = ginjector.getPopupPresenter().get();
            popupPresenter.asWidget().addAttachHandler(event -> {
                if (event.isAttached()) {
                    finishTest();
                } else {
                    fail();
                }
            });

            ginjector.getPlaceManager().revealDefaultPlace();

            Scheduler.get().scheduleDeferred(() -> presenter.showPopup(TestCase::fail));
        });
    }

    public void testPopupViewCloseHandlerIsCalledWhenHidden() {
        delayTestFinish(1000);
        runTest(() -> {
            PopupPresenterTestUtilGwt popupPresenter = ginjector.getPopupPresenter().get();

            popupPresenter.getView().onClose().subscribe(n -> finishTest());
            popupPresenter.getView().hide();
        });
    }

    /** Verify multiple name tokens. */
    public void testMultipleTokens() {
        delayTestFinish(1000);
        ginjector.getPlaceManager().revealDefaultPlace();

        runTest(() -> {
            assertTrue(ginjector.getMainPresenter().get().isVisible());
            assertFalse(ginjector.getAdminPresenter().get().isVisible());

            revealAdmin();
        });
    }

    private void revealAdmin() {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken("admin").build();
        ginjector.getPlaceManager().revealPlace(placeRequest);

        runTest(() -> {
            assertFalse(ginjector.getMainPresenter().get().isVisible());
            assertTrue(ginjector.getAdminPresenter().get().isVisible());

            revealHomePlace();
        });
    }

    private void revealHomePlace() {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken("home").build();
        ginjector.getPlaceManager().revealPlace(placeRequest);

        runTest(() -> {
            assertTrue(ginjector.getMainPresenter().get().isVisible());
            assertFalse(ginjector.getAdminPresenter().get().isVisible());

            revealSelfService();
        });
    }

    private void revealSelfService() {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken("selfService").build();
        ginjector.getPlaceManager().revealPlace(placeRequest);

        runTest(() -> {
            assertFalse(ginjector.getMainPresenter().get().isVisible());
            assertTrue(ginjector.getAdminPresenter().get().isVisible());

            finishTest();
        });
    }

    private void runTest(ScheduledCommand test) {
        Scheduler.get().scheduleDeferred(test);
    }
}
