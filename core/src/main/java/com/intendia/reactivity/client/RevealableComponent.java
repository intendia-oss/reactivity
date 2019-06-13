package com.intendia.reactivity.client;

import static io.reactivex.Completable.complete;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.intendia.qualifier.Extension;
import com.intendia.reactivity.client.Slots.RevealableSlot;
import io.reactivex.Completable;
import java.util.function.Function;

public interface RevealableComponent extends Component {
    Extension<RevealableSlot<Component>> PARENT_SLOT = Extension.key("component.parentSlot");
    Extension<Function<PlaceRequest, Completable>> PREPARE_FROM_REQUEST = Extension.key("component.prepareFromRequest");

    default RevealableSlot<Component> parentSlot() { return req(PARENT_SLOT);}

    default @CanIgnoreReturnValue Completable revealInParent() {
        return isVisible() ? complete() : parentSlot().reveal(this).compose(Slots.AsPromise);
    }
}
