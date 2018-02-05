package com.intendia.reactivity.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.intendia.reactivity.client.MainPresenterTestUtilGwt.MyView;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import javax.inject.Inject;
import javax.inject.Provider;

/** A test presenter meant to be run in a GWTTestCase. */
public class MainPresenterTestUtilGwt extends PresenterChild<MyView> {
    public static class MyView extends CompositeView implements View {
        @Inject MyView() { initWidget(new FlowPanel()); }
    }

    static class MyPlace extends Place {
        @Inject MyPlace(Provider<MainPresenterTestUtilGwt> p) { super("home", p); }
    }

    private final Provider<PopupPresenterTestUtilGwt> popup;

    @Inject MainPresenterTestUtilGwt(MyView view, RootContentSlot root, Provider<PopupPresenterTestUtilGwt> popup) {
        super(view, root);
        this.popup = popup;
    }

    public void showPopup(Runnable closeHandler) {
        PopupPresenterTestUtilGwt popup = this.popup.get();
        popup.setCloseHandler(closeHandler);
        addToPopupSlot(popup);
    }
}

