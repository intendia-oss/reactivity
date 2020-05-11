package com.intendia.reactivity.gwttest;

import com.google.gwt.user.client.ui.FlowPanel;
import com.intendia.reactivity.client.CompositeView;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PresenterChild;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import com.intendia.reactivity.gwttest.AdminPresenterTestUtilGwt.MyView;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/** A test presenter meant to be run in a GWTTestCase. */
public @Singleton class AdminPresenterTestUtilGwt extends PresenterChild<MyView> {
    public static class MyView extends CompositeView {
        @Inject MyView() { initWidget(new FlowPanel()); }
    }

    static @Singleton class MyPlace extends Place {
        @Inject MyPlace(Provider<AdminPresenterTestUtilGwt> p) { super("admin", asSingle(p)); }
    }

    @Inject AdminPresenterTestUtilGwt(MyView view, RootContentSlot root) { super(view, root); }
}

