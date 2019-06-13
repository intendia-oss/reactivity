package com.intendia.reactivity.client;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Single;

public interface Slots {

    @SuppressWarnings("unused") interface IsSlot<Child extends Component> {
        default boolean isPopup() { return this instanceof PopupSlot; }
    }

    /** A slot that can only hold one presenter. */
    interface IsSingleSlot<Child extends Component> extends IsSlot<Child> {}

    /** A slot that can reveal a child presenter. */
    interface RevealableSlot<Child extends Component> extends IsSingleSlot<Child> {
        @CanIgnoreReturnValue Completable reveal(Child child);
    }

    /** Use NestedSlot with {@link RevealableComponent}s to automatically display child presenters. */
    abstract class NestedSlot<Parent extends RevealableComponent> implements RevealableSlot<Component> {
        protected final Single<Parent> parent;
        protected NestedSlot(Single<Parent> parent) { this.parent = parent; }
        @Override public Completable reveal(Component child) {
            return parent.doOnSuccess(p -> p.setInSlot(this, child))
                    .flatMapCompletable(RevealableComponent::revealInParent).compose(AsPromise);
        }
    }

    /** A slot that can take one or many presenters. */
    class MultiSlot<Child extends Component> implements IsSlot<Child> {}

    /**
     * A slot for an ordered presenter. The presenter placed in this slot must implement comparable and will be
     * automatically placed in order in the view.
     */
    class OrderedSlot<Child extends Component & Comparable<Child>> extends MultiSlot<Child> {}

    /**
     * A slot that can take multiple PopupPresenters Acts like {@link MultiSlot} except will hide and show the
     * PopupPresenter when appropriate.
     */
    class PopupSlot<Child extends PresenterWidget<? extends PopupView>> extends MultiSlot<Child> {}

    /** A slot that can only take one presenter at a time. */
    class SingleSlot<Child extends Component> implements IsSingleSlot<Child> {}

    /** Internal utility to make reveal operations eager, so it works even if no one subscribe. */
    CompletableTransformer AsPromise = o -> o.toObservable().replay().autoConnect(-1).ignoreElements();
}
