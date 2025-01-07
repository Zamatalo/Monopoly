package com.example.frontend.views;

import com.example.frontend.components.GameBoardComponent;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route("game")
@RouteAlias("")
public class GameView extends VerticalLayout {

    public GameView() {
        GameBoardComponent component = new GameBoardComponent();
        VerticalLayout layout = new VerticalLayout(component);
        layout.add(new H3("Game"));

        add(layout, component);
    }

}
