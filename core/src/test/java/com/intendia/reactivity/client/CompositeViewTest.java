package com.intendia.reactivity.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.intendia.reactivity.client.Slots.OrderedSlot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;

@RunWith(GwtMockitoTestRunner.class)
public class CompositeViewTest {

    IsWidget widget;
    MockInsertPanel container;
    OrderedSlot<?> slot;
    TestViewImpl view;

    @Before public void init() {
        widget = mock(Widget.class);
        container = mock(MockInsertPanel.class);
        slot = mock(OrderedSlot.class);

        final Widget c = mock(Widget.class);
        when(widget.asWidget()).thenReturn(c);

        view = new TestViewImpl(widget);
        view.bindSlot(slot, container);
    }

    @Test public void test_insert_single() {
        final Widget c = mock(Widget.class);
        final ComparableContent content = new ComparableContent("TEST", c);
        view.addToSlot(slot, content);

        verify(container).insert(c, 0);
    }

    @Test public void test_insert_back() {
        final Widget a = mock(Widget.class);
        final Widget b = mock(Widget.class);
        final Widget c = mock(Widget.class);
        final ComparableContent aaa = new ComparableContent("AAA", a);
        final ComparableContent bbb = new ComparableContent("BBB", b);
        final ComparableContent ccc = new ComparableContent("CCC", c);

        view.addToSlot(slot, aaa);
        view.addToSlot(slot, bbb);
        view.addToSlot(slot, ccc);

        final InOrder inOrder = inOrder(container);
        inOrder.verify(container).insert(a, 0);
        inOrder.verify(container).insert(b, 1);
        inOrder.verify(container).insert(c, 2);
    }

    @Test public void test_insert_front() {
        final Widget a = mock(Widget.class);
        final Widget b = mock(Widget.class);
        final Widget c = mock(Widget.class);
        final ComparableContent aaa = new ComparableContent("AAA", a);
        final ComparableContent bbb = new ComparableContent("BBB", b);
        final ComparableContent ccc = new ComparableContent("CCC", c);

        view.addToSlot(slot, ccc);
        view.addToSlot(slot, bbb);
        view.addToSlot(slot, aaa);

        final InOrder inOrder = inOrder(container);
        inOrder.verify(container).insert(c, 0);
        inOrder.verify(container).insert(b, 0);
        inOrder.verify(container).insert(a, 0);
    }

    @Test public void test_insert_random_check_order() {
        final List<String> names = new ArrayList<>();
        doAnswer((Answer<Void>) invocationOnMock -> {
            final Widget w = (Widget) invocationOnMock.getArguments()[0];
            final int idx = (int) invocationOnMock.getArguments()[1];
            names.add(idx, w.getTitle());
            return null;
        }).when(container).insert(any(Widget.class), anyInt());

        for (int i = 0; i < 1000; ++i) {
            final String name = Double.toString(Math.random());
            final Widget w = mock(Widget.class);
            when(w.getTitle()).thenReturn(name);
            final ComparableContent cc = new ComparableContent(name, w);
            view.addToSlot(slot, cc);
        }

        assertTrue("Container should have 1000 members.", names.size() == 1000);
        final List<String> namesCopy = new ArrayList<>(names);
        Collections.sort(names);
        assertEquals("Container elements should be sorted.", namesCopy, names);
    }

    static class ComparableContent implements IsWidget, Comparable<ComparableContent> {
        private final String name;
        private final Widget widget;

        ComparableContent(final String name, Widget widget) {
            this.name = name;
            this.widget = widget;
        }

        @Override public int compareTo(ComparableContent o) { return name.compareTo(o.name); }
        @Override public Widget asWidget() { return widget; }
    }

    interface MockInsertPanel extends InsertPanel, HasWidgets {}

    static class TestViewImpl extends CompositeView {
        public TestViewImpl(IsWidget w) { initWidget(w.asWidget()); }
        @Override public <T extends HasWidgets & InsertPanel> void bindSlot(OrderedSlot<?> slot, T container) {
            super.bindSlot(slot, container);
        }
    }
}
