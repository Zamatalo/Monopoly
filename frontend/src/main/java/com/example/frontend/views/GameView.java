package com.example.frontend.views;

import com.example.frontend.components.GameBoardComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.util.Random;


@Route("game")
@RouteAlias("")
public class GameView extends VerticalLayout {

    public GameView() {
        GameBoardComponent component = new GameBoardComponent();
        VerticalLayout layout = new VerticalLayout(component);
        layout.setId("canvas-container");
        layout.setWidth("800px");
        layout.setHeight("800px");
        Random rand = new Random();
        Button save = new Button("Save Game", event -> {
            component.saveGameState();
        });
        save.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button load = new Button("Load Game", event -> {
            component.loadGameState();
        });
        Button movePlayerRed = new Button("Move Player", event -> {
            component.movePlayer(1, rand.nextInt(5));
        });
        Button movePlayerGreen = new Button("Move Player", event -> {
            component.movePlayer(2, rand.nextInt(5));
        });
        Button movePlayerYellow = new Button("Move Player", event -> {
            component.movePlayer(4, rand.nextInt(5));
        });
        Button movePlayerBlue = new Button("Move Player", event -> {
            component.movePlayer(3, rand.nextInt(5));
        });
        load.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        //layout.add(new H3("Game"), new HorizontalLayout(save, load, movePlayerRed,movePlayerGreen,movePlayerYellow,movePlayerBlue),component);
        layout.add(component);
        add(new H3("Game"), new HorizontalLayout(save, load, movePlayerRed, movePlayerGreen, movePlayerYellow, movePlayerBlue), layout);
    }
}





