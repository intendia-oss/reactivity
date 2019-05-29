package com.intendia.reactivity.client;

import static io.reactivex.Completable.complete;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Single;

public interface Slots {

    @SuppressWarnings("unused") interface IsSlot<T extends PresenterWidget<?>> {
        default boolean isPopup() { return this instanceof PopupSlot; }
        default boolean isRemovable() { return this instanceof RemovableSlot; }
    }

    /**
     * Only PermanentSlot does not implement this interface. Slots must implement RemovableSlot to allow presenters to
     * be removed once they are added to the slot.
     */
    interface RemovableSlot<T extends PresenterWidget<?>> extends IsSlot<T> {}

    /** A slot that can only hold one presenter. */
    interface IsSingleSlot<T extends PresenterWidget<?>> extends IsSlot<T> {}

    /** A slot that can reveal a child presenter. */
    interface RevealableSlot<T extends PresenterWidget<?>> extends RemovableSlot<T>, IsSingleSlot<T> {
        @CanIgnoreReturnValue Completable reveal(T presenter);
    }

    /** Use NestedSlot in classes extending {@link PresenterChild} to automatically display child presenters. */
    abstract class NestedSlot<T extends PresenterWidget<?>> implements RevealableSlot<T>, RemovableSlot<T> {
        protected final Single<? extends PresenterWidget<?>> adopter;
        protected NestedSlot(Single<? extends PresenterWidget<?>> adopter) { this.adopter = adopter; }
        @Override public Completable reveal(T adoptee) {
            return adopter.flatMapCompletable(p -> {
                Completable reveal = p instanceof PresenterChild ? ((PresenterChild) p).forceReveal() : complete();
                return reveal.andThen(Completable.fromAction(() -> p.setInSlot(this, adoptee)));
            }).compose(AsPromise);
        }
    }

    /** A slot that can take one or many presenters. */
    class MultiSlot<T extends PresenterWidget<?>> implements RemovableSlot<T> {}

    /**
     * A slot for an ordered presenter. The presenter placed in this slot must implement comparable and will be
     * automatically placed in order in the view.
     */
    class OrderedSlot<T extends PresenterWidget<?> & Comparable<T>> extends MultiSlot<T> {}

    /** A slot that can only take one presenter. Once a presenter is in this slot it can never be removed. */
    class PermanentSlot<T extends PresenterWidget<?>> implements IsSingleSlot<T> {}

    /**
     * A slot that can take multiple PopupPresenters Acts like {@link MultiSlot} except will hide and show the
     * PopupPresenter when appropriate.
     */
    class PopupSlot<T extends PresenterWidget<? extends PopupView>> extends MultiSlot<T> implements RevealableSlot<T> {
        protected final Single<? extends PresenterWidget<?>> proxy;
        protected PopupSlot(Single<? extends PresenterWidget<?>> proxy) { this.proxy = proxy; }
        @Override public Completable reveal(T presenter) {
            return proxy.flatMapCompletable(p -> {
                Completable reveal = p instanceof PresenterChild ? ((PresenterChild) p).forceReveal() : complete();
                return reveal.andThen(Completable.fromAction(() -> p.addToPopupSlot(presenter)));
            }).compose(AsPromise);
        }
    }

    /** A slot that can only take one presenter at a time. */
    class SingleSlot<T extends PresenterWidget<?>> implements IsSingleSlot<T>, RemovableSlot<T> {}

    /** Internal utility to make reveal operations eager, so it works even if no one subscribe. */
    CompletableTransformer AsPromise = o -> o.toObservable().replay().autoConnect(-1).ignoreElements();
}
