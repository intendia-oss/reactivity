package sample.nested.client.application;

import com.google.gwt.user.client.ui.HTML;
import com.intendia.reactivity.client.CompositeView;
import com.intendia.reactivity.client.Place;
import com.intendia.reactivity.client.PlaceRequest;
import com.intendia.reactivity.client.PresenterChild;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import sample.nested.client.NameTokens;

@Singleton
public class EmptyPresenter extends PresenterChild<EmptyPresenter.MyView> {

    public static class MyPlace extends Place {
        @Inject MyPlace(Provider<EmptyPresenter> p) {
            super(NameTokens.emptyPage, asSingle(p), (PlaceRequest request) -> true);
        }
    }

    public static class MyView extends CompositeView {
        @Inject MyView() { initWidget(new HTML("<div style='text-align: center; font-size: 120px;'>😬</div>")); }
    }

    @Inject EmptyPresenter(MyView view, ApplicationPresenter.MainContent parent) { super(view, parent); }
}
