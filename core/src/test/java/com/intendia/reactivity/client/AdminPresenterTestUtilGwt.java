package com.intendia.reactivity.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.intendia.reactivity.client.AdminPresenterTestUtilGwt.MyView;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/** A test presenter meant to be run in a GWTTestCase. */
public @Singleton class AdminPresenterTestUtilGwt extends PresenterChild<MyView> {
    public static class MyView extends CompositeView {
        @Inject MyView() { initWidget(new FlowPanel()); }
    }

    static @Singleton class MyPlace extends Place {
        @Inject MyPlace(Provider<AdminPresenterTestUtilGwt> p) { super("admin", p); }
    }

    @Inject AdminPresenterTestUtilGwt(MyView view, RootContentSlot root) { super(view, root); }
}

