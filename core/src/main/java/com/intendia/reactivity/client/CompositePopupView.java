package com.intendia.reactivity.client;

import static java.util.Objects.requireNonNull;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.intendia.reactivity.client.PopupView.PopupPositioner.CenterPopupPositioner;
import com.intendia.reactivity.client.PopupView.PopupPositioner.PopupPosition;
import com.intendia.rxgwt2.user.RxHandlers;
import io.reactivex.Observable;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A simple implementation of {@link PopupView} that can be used when the widget returned by {@link #asWidget()}
 * inherits from {@link PopupPanel}.
 */
public abstract class CompositePopupView implements PopupView {
    private HandlerRegistration autoHideHandler;

    private final Map<Slots.IsSlot, View> slots = new IdentityHashMap<>();
    private @Nullable PopupPanel popupPanel;
    private final EventBus eventBus;

    private PopupPositioner positioner;

    /**
     * By default the popup will position itself in the center of the window. To use a different positioner use {@link
     * #CompositePopupView(EventBus, PopupPositioner)} instead.
     *
     * @param eventBus The {@link EventBus}.
     */
    protected CompositePopupView(EventBus eventBus) { this(eventBus, new CenterPopupPositioner()); }

    /**
     * @param eventBus The {@link EventBus}.
     * @param positioner The {@link PopupPositioner} used to position the popup onReveal();
     * @see CenterPopupPositioner
     * @see PopupPositioner.RelativeToWidgetPopupPositioner
     * @see PopupPositioner.TopLeftPopupPositioner
     */
    protected CompositePopupView(EventBus eventBus, PopupPositioner positioner) {
        this.eventBus = eventBus;
        setPopupPositioner(positioner);

        if (repositionOnWindowResize()) {
            Window.addResizeHandler(event -> {
                if (asPopupPanel().isShowing()) {
                    showAndReposition();
                }
            });
        }
    }

    public Map<Slots.IsSlot, View> slots() { return slots; }

    @Override
    public void hide() {
        asPopupPanel().hide();
    }

    @Override
    public void setAutoHideOnNavigationEventEnabled(boolean autoHide) {
        if (autoHide) {
            if (autoHideHandler != null) {
                return;
            }
            autoHideHandler = eventBus.addHandler(PlaceManager.NavigationEvent.TYPE, navigationEvent -> hide());
        } else {
            if (autoHideHandler != null) {
                autoHideHandler.removeHandler();
            }
        }
    }

    @Override
    public Observable<?> onClose() {
        return RxHandlers.close(asPopupPanel());
    }

    @Override
    public void setPopupPositioner(PopupPositioner popupPositioner) {
        this.positioner = popupPositioner;
    }

    @Override
    public void show() {
        asPopupPanel().show();
    }

    @Override
    public void showAndReposition() {
        onReposition();
        asPopupPanel().setPopupPositionAndShow((offsetWidth, offsetHeight) -> {
            PopupPosition popupPosition = positioner.getPopupPosition(offsetWidth, offsetHeight);
            asPopupPanel().setPopupPosition(popupPosition.getLeft(), popupPosition.getTop());
        });
    }

    protected void initWidget(PopupPanel popupPanel) {
        if (this.popupPanel != null) throw new IllegalStateException("initWidget() already called");
        this.popupPanel = requireNonNull(popupPanel, "popupPanel required");
    }

    @Override public Widget asWidget() { return requireNonNull(popupPanel, "initWidget() is not called yet"); }

    protected PopupPanel asPopupPanel() { return (PopupPanel) asWidget(); }

    /**
     * Override this method to add custom logic that runs before the popup is repositioned. By default the popup will be
     * repositioned on resize and this method will be called. So you can add any resize logic here as well.
     */
    protected void onReposition() {}

    /**
     * By default PopupViews will reposition themselves when the window is resized. If you don't want the popup to be
     * repositioned or want to handle the resize yourself overide this method to return false.
     */
    protected boolean repositionOnWindowResize() { return true; }

    private void hidePopup(PopupPanel popupPanel) {
        popupPanel.hide();
    }
}
