package com.intendia.reactivity.client;

import static java.util.Objects.requireNonNull;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.intendia.reactivity.client.Slots.IsSingleSlot;
import com.intendia.reactivity.client.Slots.IsSlot;
import com.intendia.reactivity.client.Slots.MultiSlot;
import com.intendia.reactivity.client.Slots.OrderedSlot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** The interface for view classes that handles all the UI-related code for a {@link PresenterWidget}. */
public interface View {

    default Widget asWidget() { throw new UnsupportedOperationException(); }

    default Map<IsSlot, View> slots() { throw new UnsupportedOperationException(); }

    default View slot(IsSlot<?> slot) { return requireNonNull(slots().get(slot), "did you forget to call bindSlot?"); }

    default void addToSlot(MultiSlot slot, IsWidget content) {
        slot(slot).addToSlot(slot, requireNonNull(content, "content required"));
    }

    default void removeFromSlot(IsSlot slot, IsWidget content) {
        slot(slot).removeFromSlot(slot, requireNonNull(content, "content required"));
    }

    default void setInSlot(IsSingleSlot<?> slot, IsWidget content) {
        slot(slot).setInSlot(slot, requireNonNull(content, "content required"));
    }

    default void clearSlot(IsSlot<?> slot) {
        slot(slot).clearSlot(slot);
    }

    default void bindSlot(IsSingleSlot<?> slot, AcceptsOneWidget container) {
        slots().put(slot, new View() {
            @Override public void setInSlot(IsSingleSlot<?> s, IsWidget c) { container.setWidget(c); }
            @Override public void removeFromSlot(IsSlot slot, IsWidget content) { container.setWidget(null); }
            @Override public void clearSlot(IsSlot<?> slot) { container.setWidget(null); }
        });
    }

    default void bindSlot(IsSingleSlot<?> slot, HasWidgets container) {
        slots().put(slot, new View() {
            @Override public void setInSlot(IsSingleSlot<?> s, IsWidget c) {
                container.clear(); if (c != null) container.add(c.asWidget());
            }
            @Override public void removeFromSlot(IsSlot slot, IsWidget content) { container.clear(); }
            @Override public void clearSlot(IsSlot<?> slot) { container.clear(); }
        });
    }

    default void bindSlot(MultiSlot<?> slot, HasWidgets container) {
        slots().put(slot, new View() {
            @Override public void addToSlot(MultiSlot s, IsWidget content) { container.add(content.asWidget()); }
            @Override public void removeFromSlot(IsSlot s, IsWidget content) { container.remove(content.asWidget()); }
        });
    }

    default <T extends HasWidgets & InsertPanel> void bindSlot(OrderedSlot<?> slot, T container) {
        slots().put(slot, new View() {
            final List<Comparable<Comparable<?>>> list = new ArrayList<>();
            @Override public void addToSlot(MultiSlot s, IsWidget content) {
                @SuppressWarnings("unchecked") Comparable<Comparable<?>> cc = (Comparable<Comparable<?>>) content;
                final int index = Collections.binarySearch(list, cc), insertIdx = index > 0 ? index : -index - 1;
                list.add(insertIdx, cc);
                container.insert(content.asWidget(), insertIdx);
            }
            @Override public void removeFromSlot(IsSlot s, IsWidget content) {
                @SuppressWarnings("unchecked") Comparable<Comparable<?>> cc = (Comparable<Comparable<?>>) content;
                list.remove(cc);
                container.remove(content.asWidget());
            }
            @Override public void clearSlot(IsSlot<?> slot) {
                list.clear();
                container.clear();
            }
        });
    }
}
