package com.intendia.reactivity.client;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PopupPresenterTestUtilGwt extends PresenterWidget<PopupPresenterTestUtilGwt.MyView> {
    public static class MyView extends CompositePopupView {
        @Inject MyView(EventBus eventBus) { super(); initWidget(new DialogBox()); }
    }

    @Inject PopupPresenterTestUtilGwt(MyView view) {
        super(view);
    }

    public void setCloseHandler(Runnable closeHandler) {
        getView().onClose().subscribe(n -> closeHandler.run());
    }
}
