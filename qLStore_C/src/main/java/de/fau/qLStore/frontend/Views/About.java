package de.fau.qLStore.frontend.Views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "About", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
public class About extends VerticalLayout {
    public static final String VIEW_NAME = "About";

    /**
 * Little explaination of the Query Log Store
     */
    public About() {

        com.vaadin.flow.component.html.H1 heading = new H1("Welcome to Query Log Store");
        String text = "The Query Log Store is a web application for extracting, managing and analysing queries from query logs.<br>" +
                "In the Upload you can upload new query logs.<br>" +
                "In the Statistic you can find multiple statistics.<br>" +
                "In QuerySearch you can search for queries.";
        Span span = new Span();
        span.getElement().setProperty("innerHTML", text);
        add(heading, span);
    }
}

