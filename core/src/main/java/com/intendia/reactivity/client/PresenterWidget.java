package com.intendia.reactivity.client;

import static com.google.gwt.core.client.GWT.log;
import static com.intendia.rxgwt2.client.RxGwt.retryDelay;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.intendia.qualifier.Metadata;
import com.intendia.reactivity.client.Slots.IsSingleSlot;
import com.intendia.reactivity.client.Slots.IsSlot;
import com.intendia.reactivity.client.Slots.MultiSlot;
import com.intendia.reactivity.client.Slots.OrderedSlot;
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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;

public abstract class PresenterWidget<V extends View> implements Component, PopupContainer, IsWidget {
    protected boolean visible;
    private boolean isResetting;

    private final V view;
    private final Set<Component> children = new HashSet<>();
    private final SerialDisposable popup = new SerialDisposable();

    private final Mutadata data = Metadata.create();

    private final List<Disposable> revealSubscriptions = new ArrayList<>();
    private final List<Completable> revealObservables = new ArrayList<>();
    private final PublishSubject<Object> reset = PublishSubject.create();

    protected PresenterWidget(V view) {
        this.view = requireNonNull(view, "presenter view cannot be null");
    }

    @Override public @Nullable Object data(String key) { return data.data(key); }

    public V getView() { return view; }

    @Override public boolean isVisible() { return visible; }

    @Override public Widget asWidget() { return getView().asWidget(); }

    /** Ensure that PresenterWidgets may only be equal to the same instance. */
    @Override public final boolean equals(Object obj) { return super.equals(obj); }
    @Override public final int hashCode() { return super.hashCode(); }

    @Override public <T extends Component> void addToSlot(MultiSlot<? super T> slot, T child) {
        internalAddToSlot(slot, child);
    }

    @Override public <T extends Component> void setInSlot(IsSingleSlot<? super T> slot, T child) {
        internalAddToSlot(slot, child);
    }

    private <T extends Component> void internalAddToSlot(IsSlot<? super T> slot, T child) {
        requireNonNull(child, "child required");
        if (child.opt(ADOPTED).map(p -> p.by == this && p.at == slot).orElse(false)) return; // already adopted?
        // adopt child
        child.orphan();
        child.mutate().put(ADOPTED, new Papers(this, slot));
        children.add(child);

        if (slot instanceof IsSingleSlot) internalClearSlot(slot, child);
        if (!slot.isPopup()) {
            if (slot instanceof IsSingleSlot) getView().setInSlot((IsSingleSlot) slot, (IsWidget) child);
            else if (slot instanceof MultiSlot) getView().addToSlot((MultiSlot) slot, (IsWidget) child);
            else throw new UnsupportedOperationException("unsupported slot type " + slot);
        }
        if (isVisible()) {
            ((PresenterWidget) child).internalReveal();
            performReset();
        }
    }

    @Override public void clearSlot(IsSlot<?> slot) {
        internalClearSlot(slot, null);
        getView().clearSlot(slot);
    }

    private void internalClearSlot(IsSlot<?> slot, @Nullable Component dontRemove) {
        for (Component child : new ArrayList<>(children)/*copy to prevent concurrent modification*/) {
            if (Optional.ofNullable(child.papers()).map(p -> p.at == slot).orElse(false) && child != dontRemove) {
                child.orphan();
            }
        }
    }

    @Override public <T extends Component> void removeFromSlot(MultiSlot<? super T> slot, @Nullable T child) {
        if (child == null || child.opt(ADOPTED).map(o -> o.at != slot).orElse(false)) return;
        if (!child.isPopup()) getView().removeFromSlot(slot, (IsWidget) child);
        child.orphan();
    }

    @Override public void orphan() {
        @Nullable Papers papers = papers();
        if (papers == null) return;
        internalHide();
        ((PresenterWidget) papers.by).children.remove(this);
        mutate().remove(ADOPTED);
    }

    @SuppressWarnings("unchecked")
    @Override public @Nullable <T extends Component> T getChild(IsSingleSlot<T> slot) {
        for (Component c : children) if (c.req(ADOPTED).at == slot) return (T) c;
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override public <T extends Component> Set<T> getChildren(IsSlot<T> slot) {
        //noinspection SortedCollectionWithNonComparableKeys
        Set<T> result = slot instanceof OrderedSlot ? new TreeSet<>() : new HashSet<>();
        for (Component c : children) if (requireNonNull(c.papers()).at == slot) result.add((T) c);
        return result;
    }

    protected void onReveal() {}
    public void onReveal(Flowable<?> o) { onReveal(o.ignoreElements()); }
    public void onReveal(Observable<?> o) { onReveal(o.ignoreElements()); }
    public void onReveal(Single<?> o) { onReveal(o.toCompletable()); }
    public void onReveal(Maybe<?> o) { onReveal(o.ignoreElement()); }
    public void onReveal(Completable o) { revealObservables.add(o); }

    @VisibleForTesting void internalReveal() {
        if (isVisible()) return;
        onReveal();
        for (Completable o : revealObservables) subscribe(o, revealSubscriptions);
        visible = true;
        for (Component c : new ArrayList<>(children)/*prevent concurrent modification*/) {
            ((PresenterWidget) c).internalReveal();
        }
        Papers adopted = papers();
        if (adopted != null && adopted.at.isPopup()) {
            @SuppressWarnings("unchecked")
            PresenterWidget<? extends PopupView> asPopupPresenter = (PresenterWidget<? extends PopupView>) this;
            popup.set(asPopupPresenter.getView().onClose().subscribe(n -> this.orphan()));
            asPopupPresenter.getView().showAndReposition();
        }
    }

    protected void onReset() {}
    protected Observable<?> reset() { return reset; }

    @Override public void performReset() {
        if (!isVisible()) return;
        @Nullable Papers papers = papers();
        if (papers != null) {
            papers.by.performReset();
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
        for (Component c : new ArrayList<>(children)/*prevent concurrent modification*/) {
            ((PresenterWidget) c).internalReset();
        }
        if (isPopup()) ((PopupView) getView()).show();
    }

    protected void onHide() {}

    void internalHide() {
        if (!isVisible()) return;

        for (Component c : children) {
            ((PresenterWidget) c).internalHide();
        }
        if (isPopup()) {
            popup.set(Disposables.empty());
            ((PopupView) this.getView()).hide();
        }
        visible = false;
        onHide();

        for (Disposable s : revealSubscriptions) s.dispose();
        revealSubscriptions.clear();
    }

    private static CompletableTransformer subscriptionHandler = o -> o // default handler
            .doOnError(GWT::reportUncaughtException)
            .compose(retryDelay(a -> log("attach subscription error '" + a.err + "', retry attempt " + a.idx, a.err)));

    public static void registerSubscriptionHandler(CompletableTransformer impl) {
        subscriptionHandler = requireNonNull(impl, "subscription handler required");
    }

    private void subscribe(Completable o, List<Disposable> to) {
        to.add(o.compose(subscriptionHandler).subscribe());
    }
}
