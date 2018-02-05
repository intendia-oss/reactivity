package sample.nested.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface Resources extends ClientBundle {
    static void inject() {
        Resources resources = GWT.create(Resources.class);
        resources.normalize().ensureInjected();
        resources.style().ensureInjected();
    }

    interface Normalize extends CssResource {}

    interface Style extends CssResource {
        @ClassName("label_error")
        String labelError();

        String container();

        String box();

        String links();
    }

    @Source("normalize.gss")
    Normalize normalize();

    @Source("style.gss")
    Style style();
}
