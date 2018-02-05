package sample.nested.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;
import com.intendia.reactivity.client.CompositeView;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PresenterChild;
import com.intendia.reactivity.client.View;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import sample.nested.client.NameTokens;
import sample.nested.client.application.ApplicationPresenter.MainContent;

@Singleton
public class AboutUsPresenter extends PresenterChild<AboutUsPresenter.MyView> {
    public static class MyPlace extends Place {
        @Inject MyPlace(Provider<AboutUsPresenter> p) { super(NameTokens.aboutUsPage, p); }
    }

    public static class MyView extends CompositeView implements View {
        @UiTemplate("AboutUsView.ui.xml") interface Ui extends UiBinder<Widget, MyView> {
            Ui binder = GWT.create(Ui.class);
        }

        @Inject MyView() { initWidget(Ui.binder.createAndBindUi(this)); }
    }

    @Inject AboutUsPresenter(MyView view, MainContent at) { super(view, at); }
}
