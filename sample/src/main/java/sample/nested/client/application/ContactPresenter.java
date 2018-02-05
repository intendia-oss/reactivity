package sample.nested.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.intendia.reactivity.client.CompositeView;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PlaceManager;
import com.intendia.reactivity.client.PresenterChild;
import com.intendia.reactivity.client.Proxy;
import com.intendia.reactivity.client.View;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import sample.nested.client.NameTokens;
import sample.nested.client.application.ApplicationPresenter.MainContent;

@Singleton
public class ContactPresenter extends PresenterChild<ContactPresenter.MyView> {

    public static class MyProxy extends Proxy<ContactPresenter> {
        @Inject MyProxy(Provider<ContactPresenter> p, EventBus bus) {
            super(p, bus, new Place(NameTokens.contactPage));
            bus.addHandler(PlaceManager.NavigationEvent.TYPE, event -> getPresenter().subscribe(ready -> {
                // We keep track of the previously visited pages
                if (ready.navigationHistory.length() > 0) ready.navigationHistory += ", ";
                ready.navigationHistory += event.getRequest().getNameToken();
                ready.getView().setNavigationHistory(ready.navigationHistory);
            }));
        }
    }

    public static class MyView extends CompositeView implements View {
        @UiTemplate("ContactView.ui.xml") interface Ui extends UiBinder<Widget, MyView> {
            Ui binder = GWT.create(Ui.class);
        }

        @UiField Label navigationHistory;

        @Inject MyView() { initWidget(Ui.binder.createAndBindUi(this)); }

        public void setNavigationHistory(String navigationHistory) {
            this.navigationHistory.setText(navigationHistory);
        }
    }

    private String navigationHistory = "";

    @Inject ContactPresenter(MyView view, MainContent at) { super(view, at); }
}
