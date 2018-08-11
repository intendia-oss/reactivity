package sample.nested.client.application;

import static com.intendia.rxgwt2.user.RxUser.bindValueChangeOr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Widget;
import com.intendia.reactivity.client.CompositeView;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PlaceRequest;
import com.intendia.reactivity.client.PresenterChild;
import com.intendia.rxgwt2.user.RxHandlers;
import io.reactivex.Observable;
import io.reactivex.Single;
import javax.inject.Inject;
import javax.inject.Singleton;
import sample.nested.client.NameTokens;
import sample.nested.client.SampleEntryPoint.ClientModule.Presenters;

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

        @Inject MyView() { initWidget(Ui.binder.createAndBindUi(this)); }
    }

    @Inject HomePresenter(MyView view, ApplicationPresenter.MainContent parent) {
        super(view, parent);
        onReveal(Observable.combineLatest(bindValueChangeOr(view.a, 0), bindValueChangeOr(view.b, 0), (a, b) -> a + b)
                .doOnNext(c -> view.c.setText(Integer.toString(c))));
        // GWT valueBox send only events on blur, so bind keyUp and force change events to get better interactivity
        onReveal(RxHandlers.keyUp(view.a).doOnNext(ev -> ValueChangeEvent.fire(view.a, view.a.getValue())));
        onReveal(RxHandlers.keyUp(view.b).doOnNext(ev -> ValueChangeEvent.fire(view.b, view.b.getValue())));
    }
}
