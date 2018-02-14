package com.intendia.reactivity.client;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.intendia.reactivity.client.Slots.IsSingleSlot;
import com.intendia.reactivity.client.Slots.IsSlot;
import com.intendia.reactivity.client.Slots.MultiSlot;
import com.intendia.reactivity.client.Slots.OrderedSlot;
import com.intendia.reactivity.client.Slots.PopupSlot;
import com.intendia.reactivity.client.Slots.RemovableSlot;
import io.reactivex.disposables.Disposables;
import io.reactivex.disposables.SerialDisposable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;

public abstract class PresenterWidget<V extends View> implements IsWidget {
    private final PopupSlot<PresenterWidget<? extends PopupView>> POPUP_SLOT = new PopupSlot<>(null);

    PresenterWidget<?> parent;
    IsSlot<?> slot;
    boolean visible;
    boolean isResetting;

    private final V view;
    private final Set<PresenterWidget<?>> children = new HashSet<>();
    private SerialDisposable popup = new SerialDisposable();

    protected PresenterWidget(V view) {
        this.view = requireNonNull(view, "presenter view cannot be null");
    }

    public V getView() { return view; }
    public boolean isVisible() { return visible; }
    @Override public Widget asWidget() { return getView().asWidget(); }

    /** Ensure that PresenterWidgets may only be equal to the same instance. */
    @Override public final boolean equals(Object obj) { return super.equals(obj); }
    @Override public final int hashCode() { return super.hashCode(); }

    public void addToPopupSlot(PresenterWidget<? extends PopupView> child) { addToSlot(POPUP_SLOT, child); }

    public <T extends PresenterWidget<?>> void addToSlot(MultiSlot<? super T> slot, T child) {
        requireNonNull(child, "cannot add null to a slot");
        if (child.slot == slot && child.parent == this) return;

        adoptChild(slot, child);

        if (!child.isPopup()) getView().addToSlot(slot, child);
        if (isVisible()) child.internalReveal();
    }

    public void clearSlot(RemovableSlot<?> slot) {
        internalClearSlot(slot, null);
        getView().clearSlot(slot);
    }

    private void internalClearSlot(IsSlot<?> slot, @Nullable PresenterWidget<?> dontRemove) {
        for (PresenterWidget<?> child : new ArrayList<>(children)/*copy to prevent concurrent modification*/) {
            if (child.slot == slot && !child.equals(dontRemove)) child.orphan();
        }
    }

    public void removeFromParentSlot() { if (parent != null) parent.rawRemoveFromSlot(slot, this); }

    public void removeFromPopupSlot(PresenterWidget<? extends PopupView> child) { removeFromSlot(POPUP_SLOT, child); }

    public <T extends PresenterWidget<?>> void removeFromSlot(MultiSlot<? super T> slot, @Nullable T child) {
        rawRemoveFromSlot(slot, child);
    }

    private void rawRemoveFromSlot(IsSlot<?> slot, @Nullable PresenterWidget<?> child) {
        if (!slot.isRemovable()) throw new IllegalArgumentException("non removable slot");
        if (child == null || child.slot != slot) return;
        if (!child.isPopup()) getView().removeFromSlot(slot, child);
        child.orphan();
    }

    public <T extends PresenterWidget<?>> void setInSlot(IsSingleSlot<? super T> slot, T child) {
        setInSlot(slot, child, true);
    }

    public <T extends PresenterWidget<?>> void setInSlot(IsSingleSlot<? super T> slot, T child, boolean performReset) {
        requireNonNull(child, "child required");
        adoptChild(slot, child);
        internalClearSlot(slot, child);

        if (!child.isPopup()) getView().setInSlot(slot, child);

        if (isVisible()) {
            child.internalReveal();
            if (performReset) performReset();
        }
    }

    public @Nullable <T extends PresenterWidget<?>> T getChild(IsSingleSlot<T> slot) {
        for (PresenterWidget<?> c : children) if (c.slot == slot) return (T) c;
        return null;
    }

    public <T extends PresenterWidget<?>> Set<T> getChildren(IsSlot<T> slot) {
        Set<T> result = slot instanceof OrderedSlot ? new TreeSet<>() : new HashSet<>();
        for (PresenterWidget<?> c : children) if (c.slot == slot) result.add((T) c);
        return result;
    }

    /** @deprecated  */
    protected void onReveal() {}

    @VisibleForTesting void internalReveal() {
        if (isVisible()) return;

        onReveal();
        visible = true;
        for (PresenterWidget<?> c : new ArrayList<>(children)/*prevent concurrent modification*/) c.internalReveal();
        if (isPopup()) {
            @SuppressWarnings("unchecked")
            PresenterWidget<? extends PopupView> asPopupPresenter = (PresenterWidget<? extends PopupView>) this;
            popup.set(asPopupPresenter.getView().onClose().subscribe(n -> asPopupPresenter.removeFromParentSlot()));
            asPopupPresenter.getView().showAndReposition();
        }
    }

    /** @deprecated  */
    protected void onReset() {}

    public void performReset() {
        if (!isVisible()) return;
        if (parent != null) {
            parent.performReset();
        } else if (!isResetting) {
            isResetting = true;
            internalReset();
            isResetting = false;
        }
    }

    void internalReset() {
        if (!isVisible()) return;
        onReset();
        for (PresenterWidget<?> c : new ArrayList<>(children)/*prevent concurrent modification*/) c.internalReset();
        if (isPopup()) ((PopupView) getView()).show();
    }

    /** @deprecated  */
    protected void onHide() {}

    void internalHide() {
        if (!isVisible()) return;

        for (PresenterWidget<?> c : children) c.internalHide();
        if (isPopup()) {
            popup.set(Disposables.empty());
            ((PopupView) this.getView()).hide();
        }
        //unregisterVisibleHandlers();
        visible = false;
        onHide();
    }

    private <T extends PresenterWidget<?>> void adoptChild(IsSlot<T> slot, PresenterWidget<?> child) {
        if (child.parent != this) {
            if (child.parent != null) {
                if (!child.slot.isRemovable()) {
                    throw new IllegalArgumentException("Cannot move a child of a permanent slot to another slot");
                }
                child.parent.children.remove(child);
            }
            child.parent = this;
            children.add(child);
        }
        child.slot = slot;
    }

    boolean isPopup() { return slot != null && slot.isPopup(); }

    private void orphan() {
        if (slot == null) return;
        if (!slot.isRemovable()) throw new IllegalArgumentException("Cannot remove a child from a permanent slot");
        if (parent != null) {
            internalHide();
            parent.children.remove(this);
            parent = null;
        }
        slot = null;
    }
}
