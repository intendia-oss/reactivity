package com.intendia.reactivity.client;

import static io.reactivex.Completable.complete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.intendia.reactivity.client.Slots.RevealableSlot;
import io.reactivex.Completable;
import org.junit.Test;

public class PresenterTest {

    interface TestSlot extends RevealableSlot<Component> {}

    static class TestPresenter extends PresenterChild<View> {
        public int revealInParentCalled;
        TestPresenter() { super(mock(View.class), mock(TestSlot.class)); }
        @Override public Completable revealInParent() {
            if (!isVisible()) revealInParentCalled++;
            return complete();
        }
    }

    // SUT

    @Test public void forceRevealWhenPresenterIsNotVisible() {
        // Given
        TestPresenter presenter = new TestPresenter();
        assertFalse(presenter.isVisible());
        // When
        presenter.revealInParent().blockingAwait();
        // Then
        assertEquals(1, presenter.revealInParentCalled);
    }

    @Test public void forceRevealWhenPresenterIsVisible() {
        // Given
        TestPresenter presenter = new TestPresenter();
        presenter.internalReveal();
        assertTrue(presenter.isVisible());
        // When
        presenter.revealInParent().blockingAwait();
        // Then
        assertEquals(0, presenter.revealInParentCalled);
    }
}
