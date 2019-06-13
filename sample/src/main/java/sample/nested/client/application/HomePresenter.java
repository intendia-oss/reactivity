package sample.nested.client.application;

import static com.intendia.rxgwt2.user.RxUser.bindValueChangeOr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Widget;
import com.intendia.reactivity.client.CompositePopupView;
import com.intendia.reactivity.client.CompositeView;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PlaceRequest;
import com.intendia.reactivity.client.PresenterChild;
import com.intendia.reactivity.client.PresenterWidget;
import com.intendia.rxgwt2.user.RxHandlers;
import io.reactivex.Observable;
import io.reactivex.Single;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import sample.nested.client.NameTokens;
import sample.nested.client.SampleEntryPoint.ClientModule.Presenters;

@Singleton
public class HomePresenter extends PresenterChild<HomePresenter.MyView> {

    public static @Singleton class MyPlace extends Place {
        @Inject MyPlace(Single<Presenters> p) {
            super(NameTokens.homePage, p.map(Presenters::home), (PlaceRequest request) -> true);
        }
    }

    public static class MyView extends CompositeView {
        @UiTemplate("HomeView.ui.xml") interface Ui extends UiBinder<Widget, MyView> {
            Ui binder = GWT.create(Ui.class);
        }

        @UiField IntegerBox a;
        @UiField IntegerBox b;
        @UiField InlineLabel c;
        @UiField Button showPopup;

        @Inject MyView() { initWidget(Ui.binder.createAndBindUi(this)); }
    }

    @Inject HomePresenter(MyView view, ApplicationPresenter.MainContent parent, Provider<MyPopupPresenter> popup) {
        super(view, parent);
        onReveal(Observable.combineLatest(bindValueChangeOr(view.a, 0), bindValueChangeOr(view.b, 0), Integer::sum)
                .doOnNext(c -> view.c.setText(Integer.toString(c))));
        // GWT valueBox send only events on blur, so bind keyUp and force change events to get better interactivity
        onReveal(RxHandlers.keyUp(view.a).doOnNext(ev -> ValueChangeEvent.fire(view.a, view.a.getValue())));
        onReveal(RxHandlers.keyUp(view.b).doOnNext(ev -> ValueChangeEvent.fire(view.b, view.b.getValue())));
        onReveal(RxHandlers.click(view.showPopup).doOnNext(ev -> addToPopupSlot(popup.get())));
    }

    static class MyPopupPresenter extends PresenterWidget<MyPopupPresenter.MyPopupView> {

        @Inject MyPopupPresenter(MyPopupView view) {
            super(view);
            onReveal(RxHandlers.click(view.close).doOnNext(ev -> view.hide()));
        }

        static class MyPopupView extends CompositePopupView {
            final Button close;

            @Inject MyPopupView() {
                DialogBox dialog = new DialogBox(); initWidget(dialog);
                dialog.setModal(false);
                dialog.getCaption().setText("Survival dialog!️");
                FlowPanel panel = new FlowPanel(); dialog.add(panel);
                panel.add(new HTML("<p style='max-width: 400px;'>Hi! this dialog will keep open even if you navigate "
                        + "to some other place. But, it will be hide until you go back to this place. If you want to "
                        + "close it, use the close button.</p>"));
                panel.add(close = new Button("Close"));
            }
        }
    }
}
