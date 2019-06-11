package com.intendia.reactivity.client;

import static io.reactivex.Completable.complete;

import com.intendia.reactivity.client.Slots.RevealableSlot;
import io.reactivex.Completable;

public abstract class PresenterChild<V extends View> extends PresenterWidget<V> implements RevealableComponent {

    protected PresenterChild(V view, RevealableSlot<? extends Component> parentSlot) {
        super(view);
        mutate().put(RevealableComponent.PARENT_SLOT.<RevealableSlot<? extends Component>>as(), parentSlot);
        mutate().put(RevealableComponent.PREPARE_FROM_REQUEST, this::prepareFromRequest);
    }

    protected Completable prepareFromRequest(PlaceRequest request) { return complete();}
}
