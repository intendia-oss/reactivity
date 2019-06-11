package com.intendia.reactivity.client;

import com.intendia.reactivity.client.Slots.PopupSlot;

public interface PopupContainer extends Component {
    PopupSlot<PresenterWidget<? extends PopupView>> POPUP_SLOT = new PopupSlot<>();

    default void addToPopupSlot(PresenterWidget<? extends PopupView> child) {
        addToSlot(POPUP_SLOT, child);
    }

    default void removeFromPopupSlot(PresenterWidget<? extends PopupView> child) {
        removeFromSlot(POPUP_SLOT, child);
    }
}
