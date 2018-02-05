package com.intendia.reactivity.client;

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

    //TODO forceReveal/revealInParent should by async (returns RX)? it is possible that parent slot.reveal
    //end up calling a lazy loader of a code-split presenter, so this might actually force async flow
    public final void forceReveal() { if (!isVisible()) revealInParent(); }

    protected void revealInParent() { if (parentSlot != null) parentSlot.reveal(this); }

    public Completable prepareFromRequest(PlaceRequest request) { return Completable.complete(); }
}
