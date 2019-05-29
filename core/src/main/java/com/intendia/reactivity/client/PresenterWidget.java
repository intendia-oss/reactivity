package com.intendia.reactivity.client;

import static com.google.gwt.core.client.GWT.log;
import static com.intendia.rxgwt2.client.RxGwt.retryDelay;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.intendia.reactivity.client.Slots.IsSingleSlot;
import com.intendia.reactivity.client.Slots.IsSlot;
import com.intendia.reactivity.client.Slots.MultiSlot;
import com.intendia.reactivity.client.Slots.OrderedSlot;
import com.intendia.reactivity.client.Slots.PopupSlot;
import com.intendia.reactivity.client.Slots.RemovableSlot;
import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PresenterWidget<V extends View> implements IsWidget {
    private final PopupSlot<PresenterWidget<? extends PopupView>> POPUP_SLOT = new PopupSlot<>(null);

    @Nullable Papers adopted;
    protected boolean visible;
    private boolean isResetting;

    private final V view;
    private final Set<PresenterWidget<?>> children = new HashSet<>();
    private SerialDisposable popup = new SerialDisposable();

    private final List<Disposable> revealSubscriptions = new ArrayList<>();
    private final List<Completable> revealObservables = new ArrayList<>();
    private final PublishSubject<Object> reset = PublishSubject.create();

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
        requireNonNull(child, "child required");
        if (alreadyAdopted(slot, child)) return;
        adoptChild(slot, child);

        if (!child.isPopup()) getView().addToSlot(slot, child);
        if (isVisible()) child.internalReveal();
    }

    public <T extends PresenterWidget<?>> void setInSlot(IsSingleSlot<? super T> slot, T child) {
        setInSlot(slot, child, true);
    }

    public <T extends PresenterWidget<?>> void setInSlot(IsSingleSlot<? super T> slot, T child, boolean performReset) {
        requireNonNull(child, "child required");
        if (alreadyAdopted(slot, child)) return;
        adoptChild(slot, child);
        internalClearSlot(slot, child);

        if (!child.isPopup()) getView().setInSlot(slot, child);
        if (isVisible()) {
            child.internalReveal();
            if (performReset) performReset();
        }
    }

    private <T extends PresenterWidget<?>> void adoptChild(IsSlot<T> slot, PresenterWidget<?> child) {
        if (alreadyAdopted(slot, child)) return;
        if (child.adopted != null) child.orphan();
        child.adopted = new Papers(this, slot);
        children.add(child);
    }

    private <T extends PresenterWidget<?>> boolean alreadyAdopted(IsSlot<T> slot, PresenterWidget<?> child) {
        return child.adopted != null && child.adopted.by == this && child.adopted.at == slot;
    }

    public void clearSlot(RemovableSlot<?> slot) {
        internalClearSlot(slot, null);
        getView().clearSlot(slot);
    }

    private void internalClearSlot(IsSlot<?> slot, @Nullable PresenterWidget<?> dontRemove) {
        for (PresenterWidget<?> child : new ArrayList<>(children)/*copy to prevent concurrent modification*/) {
            if (child.adopted != null && child.adopted.at == slot && !child.equals(dontRemove)) child.orphan();
        }
    }

    public void removeFromParentSlot() { if (adopted != null) adopted.by.rawRemoveFromSlot(adopted.at, this); }

    public void removeFromPopupSlot(PresenterWidget<? extends PopupView> child) { removeFromSlot(POPUP_SLOT, child); }

    public <T extends PresenterWidget<?>> void removeFromSlot(MultiSlot<? super T> slot, @Nullable T child) {
        rawRemoveFromSlot(slot, child);
    }

    private void rawRemoveFromSlot(IsSlot<?> slot, @Nullable PresenterWidget<?> child) {
        if (!slot.isRemovable()) throw new IllegalArgumentException("non removable slot");
        if (child == null || (child.adopted != null && child.adopted.at != slot)) return;
        if (!child.isPopup()) getView().removeFromSlot(slot, child);
        child.orphan();
    }

    private void orphan() {
        if (adopted == null) return;
        if (!adopted.at.isRemovable()) {
            throw new IllegalArgumentException("cannot remove a child from a permanent slot");
        }
        internalHide();
        adopted.by.children.remove(this);
        adopted = null;
    }

    public @Nullable <T extends PresenterWidget<?>> T getChild(IsSingleSlot<T> slot) {
        for (PresenterWidget<?> c : children) if (requireNonNull(c.adopted).at == slot) return (T) c;
        return null;
    }

    public <T extends PresenterWidget<?>> Set<T> getChildren(IsSlot<T> slot) {
        Set<T> result = slot instanceof OrderedSlot ? new TreeSet<>() : new HashSet<>();
        for (PresenterWidget<?> c : children) if (requireNonNull(c.adopted).at == slot) result.add((T) c);
        return result;
    }

    protected void onReveal() {}

    @VisibleForTesting void internalReveal() {
        if (isVisible()) return;

        onReveal();

        for (Completable o : revealObservables) subscribe(o, revealSubscriptions);

        visible = true;
        for (PresenterWidget<?> c : new ArrayList<>(children)/*prevent concurrent modification*/) c.internalReveal();
        if (isPopup()) {
            @SuppressWarnings("unchecked")
            PresenterWidget<? extends PopupView> asPopupPresenter = (PresenterWidget<? extends PopupView>) this;
            popup.set(asPopupPresenter.getView().onClose().subscribe(n -> asPopupPresenter.removeFromParentSlot()));
            asPopupPresenter.getView().showAndReposition();
        }
    }

    public void onReveal(Flowable<?> o) { onReveal(o.ignoreElements()); }
    public void onReveal(Observable<?> o) { onReveal(o.ignoreElements()); }
    public void onReveal(Single<?> o) { onReveal(o.toCompletable()); }
    public void onReveal(Maybe<?> o) { onReveal(o.ignoreElement()); }
    public void onReveal(Completable o) { revealObservables.add(o); }

    protected void onReset() {}
    protected Observable<?> reset() { return reset; }

    public void performReset() {
        if (!isVisible()) return;
        if (adopted != null) {
            adopted.by.performReset();
        } else if (!isResetting) {
            isResetting = true;
            internalReset();
            isResetting = false;
        }
    }

    void internalReset() {
        if (!isVisible()) return;
        onReset();
        reset.onNext(this);
        for (PresenterWidget<?> c : new ArrayList<>(children)/*prevent concurrent modification*/) c.internalReset();
        if (isPopup()) ((PopupView) getView()).show();
    }

    protected void onHide() {}

    void internalHide() {
        if (!isVisible()) return;

        for (PresenterWidget<?> c : children) c.internalHide();
        if (isPopup()) {
            popup.set(Disposables.empty());
            ((PopupView) this.getView()).hide();
        }
        visible = false;
        onHide();

        for (Disposable s : revealSubscriptions) s.dispose();
        revealSubscriptions.clear();
    }

    boolean isPopup() { return adopted != null && adopted.at.isPopup(); }

    private static CompletableTransformer subscriptionHandler = o -> o // default handler
            .doOnError(GWT::reportUncaughtException)
            .compose(retryDelay(a -> log("attach subscription error '" + a.err + "', retry attempt " + a.idx, a.err)));

    public static void registerSubscriptionHandler(CompletableTransformer impl) {
        subscriptionHandler = requireNonNull(impl, "subscription handler required");
    }

    private void subscribe(Completable o, List<Disposable> to) {
        to.add(o.compose(subscriptionHandler).subscribe());
    }

    final static class Papers {
        final @Nonnull PresenterWidget<?> by;
        final @Nonnull IsSlot<?> at;
        Papers(@Nonnull PresenterWidget<?> by, @Nonnull IsSlot<?> at) { this.by = by; this.at = at; }
    }
}
