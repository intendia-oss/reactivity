package sample.nested.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MainMenu extends Composite {
    interface Ui extends UiBinder<Widget, MainMenu> {
        Ui binder = GWT.create(Ui.class);
    }

    public MainMenu() { initWidget(Ui.binder.createAndBindUi(this)); }
}
