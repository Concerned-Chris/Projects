package de.fau.tge;

import java.io.Serializable;

public class HomeViewLogic implements Serializable {

    private final HomeView view;

    public HomeViewLogic(HomeView simpleCrudView) {
        view = simpleCrudView;
    }

    public void enter(String parameter) {
    }
}
