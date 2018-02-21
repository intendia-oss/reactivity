package sample.nested.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;
import com.intendia.reactivity.client.CompositeView;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PlaceRequest;
import com.intendia.reactivity.client.PresenterChild;
import io.reactivex.Single;
import javax.inject.Inject;
import sample.nested.client.NameTokens;
import sample.nested.client.SampleEntryPoint.ClientModule.Presenters;

public class HomePresenter extends PresenterChild<HomePresenter.MyView> {

    public static class MyPlace extends Place {
        @Inject MyPlace(Single<Presenters> p) {
            super(NameTokens.homePage, p.map(Presenters::home), (PlaceRequest request) -> true);
        }
    }

    public static class MyView extends CompositeView {
        @UiTemplate("HomeView.ui.xml") interface Ui extends UiBinder<Widget, MyView> {
            Ui binder = GWT.create(Ui.class);
        }

        @Inject MyView() { initWidget(Ui.binder.createAndBindUi(this)); }
    }

    @Inject HomePresenter(MyView view, ApplicationPresenter.MainContent parent) { super(view, parent); }
}
