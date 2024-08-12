package de.fau.qLStore.frontend.Views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;

@PWA(name = "Query Log Store App",
        shortName = "qLStore App",
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
     */
    public MainView() {
//        Button home = new Button();
//        home.addClickListener(event -> UI.getCurrent().navigate(UploadView.class));
//        Tabs tabs = new Tabs();
        MenuBar menu = new MenuBar();
        menu.setOpenOnHover(true);
//        addToNavbar(home, menu);
        addToNavbar(menu);

        MenuItem about = menu.addItem("About", e -> UI.getCurrent().navigate(About.class));
        about.addComponentAsFirst(new Icon(VaadinIcon.INFO_CIRCLE));
        MenuItem uploadView = menu.addItem("Upload", e -> UI.getCurrent().navigate(UploadView.class));
        uploadView.addComponentAsFirst(new Icon(VaadinIcon.CLOUD_UPLOAD));
        MenuItem statisticView = menu.addItem("Statistic", e -> UI.getCurrent().navigate(StatisticView.class));
        statisticView.addComponentAsFirst(new Icon(VaadinIcon.TABLE));
        MenuItem querySearchView = menu.addItem("QuerySearch", e -> UI.getCurrent().navigate(QuerySearchView.class));
        querySearchView.addComponentAsFirst(new Icon(VaadinIcon.SEARCH));


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

}