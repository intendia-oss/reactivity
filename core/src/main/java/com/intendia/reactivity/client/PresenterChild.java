package com.intendia.reactivity.client;

import static io.reactivex.Completable.complete;

import com.intendia.reactivity.client.Slots.RevealableSlot;
import io.reactivex.Completable;
import javax.inject.Singleton;

@Singleton
public abstract class PresenterChild<V extends View> extends PresenterWidget<V> {
    protected final RevealableSlot<? super PresenterWidget<?>> parentSlot;

    protected PresenterChild(V view, RevealableSlot<PresenterWidget<?>> parentSlot) {
        super(view);
        this.parentSlot = parentSlot;
    }

    public final Completable forceReveal() {
        return Completable.defer(() -> !isVisible() ? revealInParent() : complete());
    }

    protected Completable revealInParent() {
        return parentSlot.reveal(this);
    }

    public Completable prepareFromRequest(PlaceRequest request) {
        return complete();
    }
}
