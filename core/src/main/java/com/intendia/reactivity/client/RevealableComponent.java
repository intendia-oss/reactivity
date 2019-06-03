package com.intendia.reactivity.client;

import static io.reactivex.Completable.complete;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.intendia.qualifier.Extension;
import com.intendia.reactivity.client.Slots.RevealableSlot;
import io.reactivex.Completable;
import java.util.function.Function;

public interface RevealableComponent extends Component {
    Extension<RevealableSlot<RevealableComponent>> PARENT_SLOT = Extension.key("component.parentSlot");
    Extension<Function<PlaceRequest, Completable>> PREPARE_FROM_REQUEST = Extension.key("component.prepareFromRequest");

    default @CanIgnoreReturnValue Completable forceReveal() {
        if (isVisible()) return complete();
        return req(PARENT_SLOT).reveal(this).compose(Slots.AsPromise);
    }
}
