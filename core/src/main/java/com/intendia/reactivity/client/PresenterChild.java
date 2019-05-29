package com.intendia.reactivity.client;

import static io.reactivex.Completable.complete;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.intendia.reactivity.client.Slots.RevealableSlot;
import io.reactivex.Completable;

public abstract class PresenterChild<V extends View> extends PresenterWidget<V> {
    protected final RevealableSlot<? super PresenterWidget<?>> parentSlot;

    protected PresenterChild(V view, RevealableSlot<PresenterWidget<?>> parentSlot) {
        super(view);
        this.parentSlot = parentSlot;
    }

    public @CanIgnoreReturnValue Completable forceReveal() {
        return !isVisible() ? revealInParent() : complete();
    }

    protected @CanIgnoreReturnValue Completable revealInParent() {
        return parentSlot.reveal(this).compose(Slots.AsPromise);
    }

    public Completable prepareFromRequest(PlaceRequest request) {
        return complete();
    }
}
