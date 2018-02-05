package com.intendia.reactivity.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.intendia.reactivity.client.Slots.MultiSlot;
import com.intendia.reactivity.client.Slots.SingleSlot;
import org.junit.Before;
import org.junit.Test;

public class PresenterWidgetTest {

    static class PresenterWidgetA extends PresenterWidgetSpy<View> {
        PresenterWidgetA(View viewA) { super(viewA); }
    }
    PresenterWidgetA presenterWidgetA() { return new PresenterWidgetA(viewA); }

    static class PresenterWidgetB extends PresenterWidgetSpy<View> {
        PresenterWidgetB(View viewB) { super(viewB); }
    }
    PresenterWidgetB presenterWidgetB() { return new PresenterWidgetB(viewB); }

    static class PresenterWidgetC extends PresenterWidgetSpy<View> {
        PresenterWidgetC(View viewC) { super(viewC); }
    }
    PresenterWidgetC presenterWidgetC() { return new PresenterWidgetC(viewC); }

    static class PresenterWidgetPopupB extends PresenterWidgetSpy<PopupView> {
        PresenterWidgetPopupB(PopupView popupB) { super(popupB); }
    }
    PresenterWidgetPopupB presenterWidgetPopupB() { return new PresenterWidgetPopupB(popupViewB); }

    static class PresenterWidgetPopupC extends PresenterWidgetSpy<PopupView> {
        PresenterWidgetPopupC(PopupView popupC) { super(popupC); }
    }
    PresenterWidgetPopupC presenterWidgetPopupC() { return new PresenterWidgetPopupC(popupViewC); }

    // Simple subclasses of PresenterWidget
    abstract static class PresenterWidgetSpy<V extends View> extends PresenterWidget<V> {
        public int onHideMethodCalled;
        public int onResetMethodCalled;
        public int onRevealMethodCalled;

        PresenterWidgetSpy(V view) {
            super(view);
        }

        @Override
        protected void onHide() {
            super.onHide();
            onHideMethodCalled++;
        }

        @Override
        protected void onReset() {
            super.onReset();
            onResetMethodCalled++;
        }

        @Override
        protected void onReveal() {
            super.onReveal();
            onRevealMethodCalled++;
        }
    }

    EventBus eventBus;
    EventBus eventBusA;

    Widget widgetA;
    Widget widgetB;
    Widget widgetC;
    Widget widgetPopupB;
    Widget widgetPopupC;
    View viewA;
    View viewB;
    View viewC;
    PopupView popupViewA;
    PopupView popupViewB;
    PopupView popupViewC;

    @Before
    public void arrange() {
        GWTMockUtilities.disarm();
        widgetA = mock(Widget.class);
        widgetB = mock(Widget.class);
        widgetC = mock(Widget.class);
        widgetPopupB = mock(Widget.class);
        widgetPopupC = mock(Widget.class);
        viewA = mock(View.class);
        viewB = mock(View.class);
        viewC = mock(View.class);
        popupViewB = mock(PopupView.class);
        popupViewC = mock(PopupView.class);

        when(viewA.asWidget()).thenReturn(widgetA);
        when(viewB.asWidget()).thenReturn(widgetB);
        when(viewC.asWidget()).thenReturn(widgetC);
        when(popupViewB.asWidget()).thenReturn(widgetPopupB);
        when(popupViewC.asWidget()).thenReturn(widgetPopupC);
    }

    @Test public void onRevealMakesPresenterWidgetVisible() {
        // Given
        PresenterWidgetA presenterWidget = presenterWidgetA();
        // When
        presenterWidget.internalReveal();
        // Then
        assertTrue(presenterWidget.isVisible());
    }

    @Test public void presenterWidgetIsInitiallyNotVisible() {
        // Given
        PresenterWidgetA presenterWidget = presenterWidgetA();
        // Then
        assertEquals(0, presenterWidget.onRevealMethodCalled);
        assertEquals(0, presenterWidget.onHideMethodCalled);
        assertFalse(presenterWidget.isVisible());
    }

    @Test public void shouldHidePopupWhenPopupPresenterRemoved() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetPopupB popupContentB = presenterWidgetPopupB();
        presenterWidgetA.internalReveal();
        // When
        presenterWidgetA.addToPopupSlot(popupContentB);
        // Then
        verify(popupContentB.getView()).showAndReposition();
        assertEquals(1, popupContentB.onRevealMethodCalled);
        assertTrue(popupContentB.isVisible());

        // and When
        presenterWidgetA.removeFromPopupSlot(popupContentB);
        // Then
        verify(popupContentB.getView()).hide();
        assertEquals(1, popupContentB.onHideMethodCalled);
        assertFalse(popupContentB.isVisible());
    }

    @Test public void testAddPopupOnInitiallyInvisiblePresenter() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetPopupB popupContentB = presenterWidgetPopupB();
        PresenterWidgetPopupC popupContentC = presenterWidgetPopupC();
        assertFalse(presenterWidgetA.isVisible());// presenterWidget is NOT visible
        // When
        presenterWidgetA.addToPopupSlot(popupContentB);
        presenterWidgetA.addToPopupSlot(popupContentC);
        // Then
        verify(popupContentB.getView(), times(0)).showAndReposition();
        verify(popupContentC.getView(), times(0)).showAndReposition();
        verify(popupContentB.getView(), times(0)).hide();
        verify(popupContentC.getView(), times(0)).hide();

        assertEquals(0, popupContentB.onRevealMethodCalled);
        assertEquals(0, popupContentC.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalReveal();
        // Then
        assertEquals(1, popupContentB.onRevealMethodCalled);
        assertEquals(1, popupContentC.onRevealMethodCalled);
        verify(popupContentB.getView()).showAndReposition();
        verify(popupContentC.getView()).showAndReposition();

        // and then When
        presenterWidgetA.internalHide();
        // Then
        assertEquals(1, popupContentB.onRevealMethodCalled);
        assertEquals(1, popupContentC.onRevealMethodCalled);
        assertEquals(1, popupContentB.onHideMethodCalled);
        assertEquals(1, popupContentC.onHideMethodCalled);
        verify(popupContentB.getView()).showAndReposition();
        verify(popupContentC.getView()).showAndReposition();
        verify(popupContentB.getView()).hide();
        verify(popupContentC.getView()).hide();
    }

    // TODO Make sure the calls happen in the right order
    // parent then child for onReveal and onReset
    // child then parent for onHide

    @Test public void testAddToSlotToSlot() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        PresenterWidgetC contentC = presenterWidgetC();
        MultiSlot<PresenterWidget<?>> slotBC = new MultiSlot<>();
        presenterWidgetA.internalReveal();

        // When
        presenterWidgetA.addToSlot(slotBC, contentB);
        presenterWidgetA.addToSlot(slotBC, contentC);
        // Then
        verify(viewA).addToSlot(slotBC, contentB);
        verify(viewA).addToSlot(slotBC, contentC);
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentC.onRevealMethodCalled);

        // and When
        presenterWidgetA.clearSlot(slotBC);
        // Then
        verify(viewA).clearSlot(slotBC);
        assertEquals(1, contentB.onHideMethodCalled);
        assertEquals(1, contentC.onHideMethodCalled);
    }

    @Test public void testClearContentInSlot() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        MultiSlot<PresenterWidget<?>> slotB = new MultiSlot<>();
        presenterWidgetA.internalReveal();
        presenterWidgetA.addToSlot(slotB, contentB);

        // When
        presenterWidgetA.clearSlot(slotB);
        // Then
        verify(viewA).clearSlot(slotB);
        assertEquals(1, contentB.onHideMethodCalled);

        // and then When
        presenterWidgetA.internalHide();
        // Then
        assertEquals(1, contentB.onHideMethodCalled);
    }

    @Test public void testPresenterWidgetCannotBeInMultipleSlots() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        MultiSlot<PresenterWidget<?>> slotA = new MultiSlot<>();
        MultiSlot<PresenterWidget<?>> slotB = new MultiSlot<>();
        presenterWidgetA.internalReveal();

        // When
        presenterWidgetA.addToSlot(slotA, contentB);
        presenterWidgetA.addToSlot(slotB, contentB);
        presenterWidgetA.removeFromSlot(slotB, contentB);
        contentB.internalReveal();
        presenterWidgetA.internalHide();
        // Then
        assertTrue(contentB.isVisible());
    }

    @Test public void testRemoveFromSlotFromSlot() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        PresenterWidgetC contentC = presenterWidgetC();
        MultiSlot<PresenterWidget<?>> slotBC = new MultiSlot<>();
        presenterWidgetA.internalReveal();
        presenterWidgetA.addToSlot(slotBC, contentB);
        presenterWidgetA.addToSlot(slotBC, contentC);

        // When
        presenterWidgetA.removeFromSlot(slotBC, contentB);
        // Then
        verify(viewA).removeFromSlot(slotBC, contentB);
        assertEquals(1, contentB.onHideMethodCalled);
        assertEquals(0, contentC.onHideMethodCalled);
    }

    @Test public void testSetInSlotHierarchyInEmptySlotOnInitiallyInvisiblePresenter1() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        PresenterWidgetC contentCinB = presenterWidgetC();
        // slot is empty in presenterWidgets, and it is NOT visible
        MultiSlot<PresenterWidget<?>> slotB = new MultiSlot<>();
        MultiSlot<PresenterWidget<?>> slotC = new MultiSlot<>();
        assertFalse(presenterWidgetA.isVisible());
        assertFalse(contentB.isVisible());

        // When
        presenterWidgetA.addToSlot(slotB, contentB);
        contentB.addToSlot(slotC, contentCinB);
        // Then
        verify(viewA).addToSlot(slotB, contentB);
        verify(viewB).addToSlot(slotC, contentCinB);
        assertEquals(0, contentB.onRevealMethodCalled);
        assertEquals(0, contentCinB.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalReveal();
        // Then
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentCinB.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalHide();
        // Then
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentCinB.onRevealMethodCalled);
        assertEquals(1, contentB.onHideMethodCalled);
        assertEquals(1, contentCinB.onHideMethodCalled);
    }

    @Test public void testSetInSlotHierarchyInEmptySlotOnInitiallyInvisiblePresenter2() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        PresenterWidgetC contentCinB = presenterWidgetC();
        // slot is empty in presenterWidgets, and it is NOT visible
        MultiSlot<PresenterWidget<?>> slotB = new MultiSlot<>();
        MultiSlot<PresenterWidget<?>> slotC = new MultiSlot<>();
        assertFalse(presenterWidgetA.isVisible());
        assertFalse(contentB.isVisible());

        // When
        contentB.addToSlot(slotC, contentCinB);
        // Then
        verify(viewB).addToSlot(slotC, contentCinB);
        assertEquals(0, contentCinB.onRevealMethodCalled);

        // and then When
        presenterWidgetA.addToSlot(slotB, contentB);
        // Then
        verify(viewA).addToSlot(slotB, contentB);
        assertEquals(0, contentB.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalReveal();
        // Then
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentCinB.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalHide();
        // Then
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentCinB.onRevealMethodCalled);
        assertEquals(1, contentB.onHideMethodCalled);
        assertEquals(1, contentCinB.onHideMethodCalled);
    }

    @Test public void testSetInSlotHierarchyInEmptySlotOnInitiallyVisiblePresenter() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        PresenterWidgetC contentCinB = presenterWidgetC();
        MultiSlot<PresenterWidget<?>> slotB = new MultiSlot<>();
        MultiSlot<PresenterWidget<?>> slotC = new MultiSlot<>();
        presenterWidgetA.internalReveal();

        // When
        presenterWidgetA.addToSlot(slotB, contentB);
        contentB.addToSlot(slotC, contentCinB);
        // Then
        verify(viewA).addToSlot(slotB, contentB);
        verify(viewB).addToSlot(slotC, contentCinB);
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentCinB.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalHide();
        // Then
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentCinB.onRevealMethodCalled);
        assertEquals(1, contentB.onHideMethodCalled);
        assertEquals(1, contentCinB.onHideMethodCalled);
    }

    @Test public void testSetInSlotInEmptySlotOnInitiallyInvisiblePresenter() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        PresenterWidgetC contentC = presenterWidgetC();
        // slot is empty in presenterWidget, and it is NOT visible
        MultiSlot<PresenterWidget<?>> slotB = new MultiSlot<>();
        MultiSlot<PresenterWidget<?>> slotC = new MultiSlot<>();
        assertFalse(presenterWidgetA.isVisible());

        // When
        presenterWidgetA.addToSlot(slotB, contentB);
        presenterWidgetA.addToSlot(slotC, contentC);
        // Then
        verify(viewA).addToSlot(slotB, contentB);
        verify(viewA).addToSlot(slotC, contentC);
        assertEquals(0, contentB.onRevealMethodCalled);
        assertEquals(0, contentC.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalReveal();
        // Then
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentC.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalHide();
        // Then
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentC.onRevealMethodCalled);
        assertEquals(1, contentB.onHideMethodCalled);
        assertEquals(1, contentC.onHideMethodCalled);
    }

    @Test public void testSetInSlotInEmptySlotOnInitiallyVisiblePresenter() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        PresenterWidgetC contentC = presenterWidgetC();
        MultiSlot<PresenterWidget<?>> slotB = new MultiSlot<>();
        MultiSlot<PresenterWidget<?>> slotC = new MultiSlot<>();
        presenterWidgetA.internalReveal();

        // When
        presenterWidgetA.addToSlot(slotB, contentB);
        presenterWidgetA.addToSlot(slotC, contentC);
        // Then
        verify(viewA).addToSlot(slotB, contentB);
        verify(viewA).addToSlot(slotC, contentC);

        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentC.onRevealMethodCalled);

        // and then When
        presenterWidgetA.internalHide();
        // Then
        assertEquals(1, contentB.onRevealMethodCalled);
        assertEquals(1, contentC.onRevealMethodCalled);
        assertEquals(1, contentB.onHideMethodCalled);
        assertEquals(1, contentC.onHideMethodCalled);
    }

    @Test(expected = IllegalArgumentException.class) public void test_set_null_content_throws_exception() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB contentB = presenterWidgetB();
        SingleSlot<PresenterWidget<?>> slotB = new SingleSlot<>();
        presenterWidgetA.internalReveal();
        presenterWidgetA.setInSlot(slotB, contentB);

        // When
        presenterWidgetA.setInSlot(slotB, null);
        // Then
        fail("should throw exception");
    }

    @Test public void testSwitchPopupToAnotherPresenter1() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB presenterWidgetB = presenterWidgetB();
        PresenterWidgetPopupC popupContentC = presenterWidgetPopupC();
        presenterWidgetA.internalReveal();
        presenterWidgetB.internalReveal();
        presenterWidgetA.addToPopupSlot(popupContentC);

        // When
        presenterWidgetB.addToPopupSlot(popupContentC);
        presenterWidgetB.internalHide();
        // Then
        assertFalse(popupContentC.isVisible());
    }

    @Test public void testSwitchPopupToAnotherPresenter2() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB presenterWidgetB = presenterWidgetB();
        PresenterWidgetPopupC popupContentC = presenterWidgetPopupC();
        presenterWidgetA.internalReveal();
        presenterWidgetB.internalReveal();
        presenterWidgetA.addToPopupSlot(popupContentC);

        // When
        presenterWidgetB.addToPopupSlot(popupContentC);
        presenterWidgetB.internalHide();
        presenterWidgetA.addToPopupSlot(popupContentC);
        // Then
        assertTrue(popupContentC.isVisible());
    }

    @Test public void testSwitchPresenterWidgetToAnotherPresenter1() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB presenterWidgetB = presenterWidgetB();
        PresenterWidgetC contentC = presenterWidgetC();
        MultiSlot<PresenterWidget<?>> slotCinA = new MultiSlot<>();
        MultiSlot<PresenterWidget<?>> slotCinB = new MultiSlot<>();
        presenterWidgetA.internalReveal();
        presenterWidgetB.internalReveal();

        // When
        presenterWidgetA.addToSlot(slotCinA, contentC);
        presenterWidgetB.addToSlot(slotCinB, contentC);
        presenterWidgetB.internalHide();
        // Then
        assertFalse(contentC.isVisible());
    }

    @Test public void testSwitchPresenterWidgetToAnotherPresenter2() {
        // Given
        PresenterWidgetA presenterWidgetA = presenterWidgetA();
        PresenterWidgetB presenterWidgetB = presenterWidgetB();
        PresenterWidgetC contentC = presenterWidgetC();
        MultiSlot<PresenterWidget<?>> slotCinA = new MultiSlot<>();
        MultiSlot<PresenterWidget<?>> slotCinB = new MultiSlot<>();
        presenterWidgetA.internalReveal();
        presenterWidgetB.internalReveal();

        // When
        presenterWidgetA.addToSlot(slotCinA, contentC);
        presenterWidgetB.addToSlot(slotCinB, contentC);
        presenterWidgetB.internalHide();
        presenterWidgetA.addToSlot(slotCinA, contentC);
        // Then
        assertTrue(contentC.isVisible());
    }
}
