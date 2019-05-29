package com.intendia.reactivity.client;

import static com.intendia.reactivity.client.Slots.AsPromise;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.intendia.reactivity.client.PlaceManager.LockInteractionEvent;
import com.intendia.reactivity.client.Slots.IsSlot;
import com.intendia.reactivity.client.Slots.RevealableSlot;
import io.reactivex.Completable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class RootPresenter extends PresenterWidget<RootPresenter.RootView> {

    public static class RootView implements View {
        private final Map<IsSlot, View> slots = new HashMap<>();
        private @Nullable Element glass;

        @Inject public RootView(RootContentSlot rootSlot) { bindSlot(rootSlot, RootPanel.get()); }

        @Override public Map<IsSlot, View> slots() { return slots; }

        public Element ensureGlass() {
            if (glass == null) {
                glass = Document.get().createDivElement();
                Style style = glass.getStyle();
                style.setPosition(Position.ABSOLUTE);
                style.setLeft(0, Unit.PX);
                style.setTop(0, Unit.PX);
                style.setRight(0, Unit.PX);
                style.setBottom(0, Unit.PX);
                style.setZIndex(2147483647); // Maximum z-index
                style.setBackgroundColor("#FFFFFF");
                style.setOpacity(0);
            }
            return glass;
        }

        public void lockScreen(boolean lock) {
            if (lock) Document.get().getBody().appendChild(ensureGlass());
            else if (glass != null) glass.removeFromParent();
        }
    }

    public static @Singleton class RootContentSlot implements RevealableSlot<PresenterWidget<?>> {
        private final Provider<RootPresenter> root;
        @Inject RootContentSlot(Provider<RootPresenter> root) { this.root = root; }
        @Override public Completable reveal(PresenterWidget<?> presenter) {
            return Completable.fromAction(() -> root.get().setInSlot(this, presenter)).compose(AsPromise);
        }
    }

    public static @Singleton class RootPopupSlot implements RevealableSlot<PresenterWidget<? extends PopupView>> {
        private final Provider<RootPresenter> root;
        @Inject RootPopupSlot(Provider<RootPresenter> root) { this.root = root; }
        @Override public Completable reveal(PresenterWidget<? extends PopupView> presenter) {
            return Completable.fromAction(() -> root.get().addToPopupSlot(presenter)).compose(AsPromise);
        }
    }

    @Inject public RootPresenter(EventBus bus, RootView view) {
        super(view);
        visible = true;
        bus.addHandler(LockInteractionEvent.TYPE, e -> getView().lockScreen(e.shouldLock()));
    }
}
