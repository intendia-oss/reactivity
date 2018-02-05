package sample.nested.client;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import io.reactivex.Single;

public interface AsyncLoader {
    static <P> Single<P> asSingle(ListenableFuture<P> p, EventBus bus) {
        return Single.<P>create(em -> {
            bus.fireEvent(new AsyncCallStartEvent());
            Futures.addCallback(p, new FutureCallback<P>() {
                @Override public void onSuccess(P result) {
                    bus.fireEvent(new AsyncCallSucceedEvent());
                    em.onSuccess(result);
                }
                @Override public void onFailure(Throwable ex) {
                    bus.fireEvent(new AsyncCallFailEvent(ex));
                    em.onError(ex);
                }
            }, MoreExecutors.directExecutor());
        }).cache();
    }

    class AsyncCallFailEvent extends GwtEvent<AsyncCallFailEvent.AsyncCallFailHandler> {
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

    class AsyncCallStartEvent extends GwtEvent<AsyncCallStartEvent.AsyncCallStartHandler> {
        public static final Type<AsyncCallStartHandler> TYPE = new Type<>();
        AsyncCallStartEvent() {}
        @Override public Type<AsyncCallStartHandler> getAssociatedType() { return TYPE; }
        @Override protected void dispatch(AsyncCallStartHandler handler) { handler.onAsyncCallStart(this); }
        public interface AsyncCallStartHandler extends EventHandler {
            void onAsyncCallStart(final AsyncCallStartEvent asyncCallStartEvent);
        }
    }

    class AsyncCallSucceedEvent extends GwtEvent<AsyncCallSucceedEvent.AsyncCallSucceedHandler> {
        public static final Type<AsyncCallSucceedHandler> TYPE = new Type<>();
        AsyncCallSucceedEvent() {}
        @Override public Type<AsyncCallSucceedHandler> getAssociatedType() { return TYPE; }
        @Override protected void dispatch(AsyncCallSucceedHandler handler) { handler.onAsyncCallSucceed(this); }
        public interface AsyncCallSucceedHandler extends EventHandler {
            void onAsyncCallSucceed(final AsyncCallSucceedEvent asyncCallSucceedEvent);
        }
    }
}
