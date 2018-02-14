package sample.nested.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.intendia.reactivity.client.CompositeView;
import com.intendia.reactivity.client.PlaceManager;
import com.intendia.reactivity.client.PresenterChild;
import com.intendia.reactivity.client.PresenterWidget;
import com.intendia.reactivity.client.RootPresenter.RootContentSlot;
import com.intendia.reactivity.client.Slots.NestedSlot;
import com.intendia.reactivity.client.View;
import dagger.Lazy;
import io.reactivex.Single;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This is the top-level presenter of the hierarchy. Other presenters reveal themselves within this presenter.
 * <p/>
 * The goal of this sample is to show how to use nested presenters. These can be useful to decouple multiple presenters
 * that need to be displayed on the screen simultaneously.
 */
@Singleton
public class ApplicationPresenter extends PresenterChild<ApplicationPresenter.MyView> {

    /**
     * This is the top-level view of the application. Every time another presenter wants to reveal itself, {@link
     * MyView} will add its content of the target inside the {@code mainContantPanel}.
     */
    public static class MyView extends CompositeView implements View {
        @UiTemplate("ApplicationView.ui.xml") interface Ui extends UiBinder<Widget, MyView> {
            Ui binder = GWT.create(Ui.class);
        }

        @UiField SimplePanel mainContentPanel;
        @UiField Element loadingMessage;

        @Inject MyView(MainContent slot) {
            initWidget(Ui.binder.createAndBindUi(this));
            bindSlot(slot, (HasOneWidget) mainContentPanel);
        }

        public void showLoading(boolean visible) {
            loadingMessage.getStyle().setVisibility(visible ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
        }
    }

    public static @Singleton class MainContent extends NestedSlot<PresenterWidget<?>> {
        @Inject MainContent(Lazy<ApplicationPresenter> presenter) { super(Single.fromCallable(presenter::get)); }
    }

    @Inject ApplicationPresenter(MyView view, RootContentSlot root, EventBus bus) {
        super(view, root);
        bus.addHandler(PlaceManager.LockInteractionEvent.TYPE, e -> getView().showLoading(e.shouldLock()));
    }
}
