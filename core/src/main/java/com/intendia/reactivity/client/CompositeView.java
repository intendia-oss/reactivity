package com.intendia.reactivity.client;

import com.google.gwt.user.client.ui.Composite;
import com.intendia.reactivity.client.Slots.IsSlot;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class CompositeView extends Composite implements View {
    private final Map<IsSlot, View> slots = new IdentityHashMap<>();
    public Map<IsSlot, View> slots() { return slots; }
}
