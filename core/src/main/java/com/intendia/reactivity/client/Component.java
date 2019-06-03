package com.intendia.reactivity.client;

import com.intendia.qualifier.Extension;
import com.intendia.qualifier.Metadata;
import com.intendia.reactivity.client.Slots.IsSingleSlot;
import com.intendia.reactivity.client.Slots.IsSlot;
import com.intendia.reactivity.client.Slots.MultiSlot;
import com.intendia.reactivity.client.Slots.RemovableSlot;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Component extends Metadata {
    Extension<Papers> ADOPTED = Extension.key("component.adopted");

    void performReset();

    boolean isVisible();
    default boolean isPopup() { Papers adopted = papers(); return adopted != null && adopted.at.isPopup(); }
    default @Nullable Papers papers() { return data(ADOPTED); }

    // Slots
    void addToPopupSlot(PresenterWidget<? extends PopupView> child);
    <T extends Component> void addToSlot(MultiSlot<? super T> slot, T child);
    <T extends Component> void setInSlot(IsSingleSlot<? super T> slot, T child);
    <T extends Component> void setInSlot(IsSingleSlot<? super T> slot, T child, boolean performReset);
    void clearSlot(RemovableSlot<?> slot);
    void removeFromParentSlot();
    void removeFromPopupSlot(PresenterWidget<? extends PopupView> child);
    <T extends Component> void removeFromSlot(MultiSlot<? super T> slot, @Nullable T child);
    @Nullable <T extends Component> T getChild(IsSingleSlot<T> slot);
    <T extends Component> Set<T> getChildren(IsSlot<T> slot);

    final class Papers {
        final @Nonnull PresenterWidget<?> by;
        final @Nonnull IsSlot<?> at;
        Papers(@Nonnull PresenterWidget<?> by, @Nonnull IsSlot<?> at) { this.by = by; this.at = at; }
    }
}
