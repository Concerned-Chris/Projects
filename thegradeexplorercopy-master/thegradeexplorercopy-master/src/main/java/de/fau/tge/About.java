package de.fau.tge;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.awt.*;

@Route(value = "About", layout = MainView.class)
@RouteAlias(value = "About", layout = MainView.class)
public class About extends VerticalLayout {
    public static final String VIEW_NAME = "About";

    /**
     * Little explaination of The Grade Explorer
     */
    public About() {
        Text about = new Text("The Grade Explorer is a Website to help managing Exams and their belonging properties.");
        Image img = new Image("icons/logo.png", "Grade Explorer Logo");
        img.setHeight("40vh");
        img.setWidth("35vh");

        add(about, img);
    }
}
