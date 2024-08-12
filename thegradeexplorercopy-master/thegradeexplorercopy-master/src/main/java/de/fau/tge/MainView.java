package de.fau.tge;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MainView
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class first of all creates the navbar.
 * With it the user is able to navigate back to the HomeView wherever he is.
 */

@PWA(name = "The Great Explorer Application",
        shortName = "TGE App",
        description = "Version 0.4 of the Targaryen Milestone",
        enableInstallPrompt = false)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/menu-buttons.css", themeFor = "vaadin-button")
public class MainView extends AppLayout implements RouterLayout {

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     * @param service The message service. Automatically injected Spring managed bean.
     */
    public MainView(@Autowired GreetService service) {
        Image img = new Image("icons/logo.png", "Grade Explorer Logo");
        Button home = new Button(img);
        home.setHeight("110px");
        img.setHeight("100px");
        img.setWidth("95px");
        home.addClickListener(event -> UI.getCurrent().navigate(HomeView.class));
//        Tabs tabs = new Tabs();
        MenuBar menu = new MenuBar();
        menu.setOpenOnHover(true);
        addToNavbar(home, menu);

        MenuItem homeView = menu.addItem("Home", e -> UI.getCurrent().navigate(HomeView.class));
        homeView.addComponentAsFirst(new Icon(VaadinIcon.HOME));
        MenuItem about = menu.addItem("About", e -> UI.getCurrent().navigate(About.class));
        about.addComponentAsFirst(new Icon(VaadinIcon.INFO_CIRCLE));

//        createRouterLink(HomeView.class, " Home", VaadinIcon.HOME.create(), tabs);
//        //createRouterLink(ProjecdView.class, " Projects", VaadinIcon.LIST.create(), tabs);
//        createRouterLink(About.class, " About", VaadinIcon.INFO_CIRCLE.create(), tabs);
    }

    private void createRouterLink(Class<? extends Component> ViewClass, String caption, Icon icon, Tabs tabs) {
        RouterLink link = new RouterLink(null,ViewClass);
        link.add(icon);
        icon.getStyle().set("marginRight", "5px");
        link.add(caption);
        Tab tab = new Tab();
        tab.add(link);
        tabs.add(tab);
    }

    /*@Override
    public void sessionDestroy(SessionDestroyEvent event) {
        dataset.close();
    }*/

}
