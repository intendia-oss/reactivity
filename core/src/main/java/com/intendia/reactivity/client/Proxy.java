package com.intendia.reactivity.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import io.reactivex.Single;
import javax.inject.Provider;

public class Proxy<P extends PresenterWidget<?>> {
    protected final Single<P> presenter;
    protected final Place place;

    public Proxy(Provider<P> p, EventBus bus, Place place) {
        this.presenter = Single
                .<P>create(source -> GWT.runAsync(new RunAsyncCallback() {
                    @Override public void onFailure(Throwable reason) { source.onError(reason); }
                    @Override public void onSuccess() { source.onSuccess(p.get()); }
                }))
                .doOnSubscribe(s -> bus.fireEvent(new AsyncCallStartEvent()))
                .doOnSuccess(n -> bus.fireEvent(new AsyncCallSucceedEvent()))
                .doOnError(ex -> bus.fireEvent(new AsyncCallFailEvent(ex)))
                .cache();
        this.place = place;
    }

    public Place getPlace() { return place; }
    public Single<P> getPresenter() { return presenter; }

    public static class AsyncCallFailEvent extends GwtEvent<Proxy.AsyncCallFailEvent.AsyncCallFailHandler> {
        public static final Type<AsyncCallFailHandler> TYPE = new Type<>();
        private final Throwable caught;
        AsyncCallFailEvent(Throwable caught) { this.caught = caught; }
        @Override public Type<AsyncCallFailHandler> getAssociatedType() { return TYPE; }
        public Throwable getCaught() { return caught; }
        @Override protected void dispatch(AsyncCallFailHandler handler) { handler.onAsyncCallFail(this); }
        public interface AsyncCallFailHandler extends EventHandler {
            void onAsyncCallFail(final AsyncCallFailEvent asyncCallFailEvent);
        }
    }

    public static class AsyncCallStartEvent extends GwtEvent<Proxy.AsyncCallStartEvent.AsyncCallStartHandler> {
        public static final Type<AsyncCallStartHandler> TYPE = new Type<>();
        AsyncCallStartEvent() {}
        @Override public Type<AsyncCallStartHandler> getAssociatedType() { return TYPE; }
        @Override protected void dispatch(AsyncCallStartHandler handler) { handler.onAsyncCallStart(this); }
        public interface AsyncCallStartHandler extends EventHandler {
            void onAsyncCallStart(final AsyncCallStartEvent asyncCallStartEvent);
        }
    }

    public static class AsyncCallSucceedEvent extends GwtEvent<Proxy.AsyncCallSucceedEvent.AsyncCallSucceedHandler> {
        public static final Type<AsyncCallSucceedHandler> TYPE = new Type<>();
        AsyncCallSucceedEvent() {}
        @Override public Type<AsyncCallSucceedHandler> getAssociatedType() { return TYPE; }
        @Override protected void dispatch(AsyncCallSucceedHandler handler) { handler.onAsyncCallSucceed(this); }
        public interface AsyncCallSucceedHandler extends EventHandler {
            void onAsyncCallSucceed(final AsyncCallSucceedEvent asyncCallSucceedEvent);
        }
    }
}
