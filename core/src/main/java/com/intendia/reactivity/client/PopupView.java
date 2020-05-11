package com.intendia.reactivity.client;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import io.reactivex.Observable;

/**
 * The interface for {@link View} classes that are meant to be displayed as popup, like a GWT {@link
 * PopupPanel PopupPanel} or a {@link com.google.gwt.user.client.ui.DialogBox DialogBox}.
 */
public interface PopupView extends View {
    /**
     * <b>Important!</b> Do not call this directly. Instead use {@link PresenterWidget#addToPopupSlot(PresenterWidget)},
     * passing this view's {@link PresenterWidget}.
     *
     * <p>Make sure the {@link PopupView} is visible. Will not reposition the popup before showing it.</p>
     */
    void show();

    /**
     * You don't need to call this directly. It is automatically called during {@link PresenterWidget#onReveal()}. Will
     * position the popup before showing it.
     */
    void showAndReposition();

    /** Make sure the {@link PopupView} is hidden. You can call this method directly. */
    void hide();

    Observable<?> onClose();

    /**
     * Indicates that the view should automatically hide when a {@link PlaceManager.NavigationEvent} is fired. This is
     * better
     * than using GWT's {@link PopupPanel#setAutoHideOnHistoryEventsEnabled(boolean)} since the latter will
     * automatically hide the dialog even if navigation is refused through
     * {@link PlaceManager#setOnLeaveConfirmation(String) PlaceManager#setOnLeaveConfirmation} .
     */
    void setAutoHideOnNavigationEventEnabled(EventBus eventBus, boolean autoHide);

    void setPopupPositioner(PopupPositioner positioner);

    abstract class PopupPositioner {

        public PopupPosition getPopupPosition(int popupWidth, int popupHeight) {
            return new PopupPosition(getLeft(popupWidth), getTop(popupHeight));
        }

        protected abstract int getLeft(int popupWidth);

        protected abstract int getTop(int popupHeight);

        public static class PopupPosition {
            private final int top;
            private final int left;

            public PopupPosition(int left, int top) {
                this.left = left;
                this.top = top;
            }

            public int getLeft() {
                return left;
            }

            public int getTop() {
                return top;
            }
        }

        /** Positions the popup at the left and top coordinates. */
        public static class TopLeftPopupPositioner extends PopupPositioner {
            private final int left;
            private final int top;

            public TopLeftPopupPositioner(int left, int top) {
                super();
                this.left = left;
                this.top = top;
            }

            @Override
            protected int getLeft(int popupWidth) {
                return left;
            }

            @Override
            protected int getTop(int popupHeight) {
                return top;
            }
        }

        /** Positions the popup relative to a widget. */
        public static class RelativeToWidgetPopupPositioner extends PopupPositioner {
            private final Widget widget;
            private final boolean clipToWindow;

            /**
             * @param widget - the widget relative to which the popup will be shown.
             *
             * <p>If there is enough space to the right, the left edge of the popup will be positioned flush with
             * the left edge of the widget.</p>
             * <pre>
             *     --------
             *     |widget|
             *     -------------
             *     |popup panel|
             *     -------------
             * </pre>
             *
             * <p>Otherwise if there is enough space on the left the right edge of the popup will be positioned
             * flush with the right edge of the widget.</p>
             * <pre>
             *          --------
             *          |widget|
             *     -------------
             *     |popup panel|
             *     -------------
             * </pre>
             *
             * <p>If there is not enough space to the left or the right and clipToWindow is on. The popup will be
             * positioned on the left edge of the screen.</p>
             * <pre>
             *      |   --------
             *      |   |widget|
             *      |-------------
             *      ||popup panel|
             *      |-------------
             * </pre>
             *
             * If you would prefer the popupPanel to always be flush with the widget call
             * {@link #RelativeToWidgetPopupPositioner(IsWidget, boolean)}
             * and set clipToWindow to false
             */
            public RelativeToWidgetPopupPositioner(IsWidget widget) {
                this(widget, true);
            }

            /**
             * @param widget - the widget relative to which the popup will be shown.
             * @param clipToWindow - set to false to always position the popup flush to an edge of the widget.
             *
             * <p>If there is enough space to the right, the left edge of the popup will be positioned flush with
             * the left edge of the widget.</p>
             * <pre>
             *     --------
             *     |widget|
             *     -------------
             *     |popup panel|
             *     -------------
             * </pre>
             *
             * <p>Otherwise if there is enough space on the left the right edge of the popup will be positioned
             * flush with the right edge of the widget.</p>
             * <pre>
             *          --------
             *          |widget|
             *     -------------
             *     |popup panel|
             *     -------------
             * </pre>
             *
             * <p>If there is not enough space to the left or the right and clipToWindow is on. The popup will be
             * positioned on the left edge of the screen.</p>
             * <pre>
             *      |   --------
             *      |   |widget|
             *      |-------------
             *      ||popup panel|
             *      |-------------
             * </pre>
             *
             * Set clipToWindow to false to always position the popup flush to an edge of the widget and expand
             * the screen when it will not fit.
             */
            public RelativeToWidgetPopupPositioner(IsWidget widget, boolean clipToWindow) {
                this.widget = widget.asWidget();
                this.clipToWindow = clipToWindow;
            }

            @Override
            protected int getTop(int popupHeight) {
                int top = widget.getAbsoluteTop();

                int windowTop = Window.getScrollTop();
                int windowBottom = Window.getScrollTop() + Window.getClientHeight();

                int distanceFromWindowTop = top - windowTop;

                int distanceToWindowBottom = windowBottom - (top + widget.getOffsetHeight());

                if (distanceToWindowBottom < popupHeight && distanceFromWindowTop >= popupHeight) {
                    top -= popupHeight;
                } else {
                    top += widget.getOffsetHeight();
                }
                return top;
            }

            @Override
            protected int getLeft(int popupWidth) {
                return LocaleInfo.getCurrentLocale().isRTL() ? getRtlLeft(popupWidth) : getLtrLeft(popupWidth);
            }

            protected int getLtrLeft(int popupWidth) {
                if (!canFitOnLeftEdge(popupWidth) && (canFitOnRightEdge(popupWidth) || clipToWindow)) {
                    return Math.max(0, getRightEdge(popupWidth));
                } else {
                    return widget.getAbsoluteLeft();
                }
            }

            protected int getRtlLeft(int popupWidth) {
                if (!canFitOnRightEdge(popupWidth) && (canFitOnLeftEdge(popupWidth) || !clipToWindow)) {
                    return widget.getAbsoluteLeft();
                } else {
                    return Math.max(0, getRightEdge(popupWidth));
                }
            }

            private int getRightEdge(int popupWidth) {
                return widget.getAbsoluteLeft() + widget.getOffsetWidth() - popupWidth;
            }

            private boolean canFitOnLeftEdge(int popupWidth) {
                int windowRight = Window.getClientWidth() + Window.getScrollLeft();
                return windowRight - popupWidth > widget.getAbsoluteLeft();
            }

            private boolean canFitOnRightEdge(int popupWidth) {
                return getRightEdge(popupWidth) >= Window.getScrollLeft();
            }
        }

        public static class CenterPopupPositioner extends PopupPositioner {
            /** By default this method centers the popup horizontally. */
            protected int getLeft(int popupWidth) {
                return ((Window.getClientWidth() - popupWidth) / 2) + Window.getScrollLeft();
            }

            /** By default this method centers the popup vertically. */
            protected int getTop(int popupHeight) {
                return ((Window.getClientHeight() - popupHeight) / 2) + Window.getScrollTop();
            }
        }
    }
}
