package com.intendia.reactivity.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.intendia.reactivity.client.AdminPresenterTestUtilGwt.MyView;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import javax.inject.Inject;
import javax.inject.Provider;

/** A test presenter meant to be run in a GWTTestCase. */
public class AdminPresenterTestUtilGwt extends PresenterChild<MyView> {
    public static class MyView extends CompositeView {
        @Inject MyView() { initWidget(new FlowPanel()); }
    }

    static class MyProxy extends Proxy<AdminPresenterTestUtilGwt> {
        @Inject MyProxy(Provider<AdminPresenterTestUtilGwt> p, EventBus bus) {
            super(p, bus, new Place("admin"));// "selfService"
        }
    }

    @Inject AdminPresenterTestUtilGwt(MyView view, RootContentSlot root) { super(view, root); }
}

