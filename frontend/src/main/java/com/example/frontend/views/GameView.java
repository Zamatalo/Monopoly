package com.example.frontend.views;

import com.example.frontend.components.GameBoardComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;


@Route("game")
@RouteAlias("")
public class GameView extends VerticalLayout {
    GameBoardComponent component = new GameBoardComponent();
    public GameView() {
        setSizeFull();
        initButtons();

        add(component);
    }

    public void initButtons() {
        HorizontalLayout controls = new HorizontalLayout();
        controls.setId("controls");

        Button save = new Button("Save Game", event -> {
            component.saveGameState();
        });
        save.addClassName("controlsButton");
        save.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button load = new Button("Load Game", event -> {
            component.loadGameState();
        });
        load.addClassName("controlsButton");
        load.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button movePlayerRed = new Button("Move Player Red");
        movePlayerRed.addClassName("controlsButton");
        movePlayerRed.addClickListener(event -> {
            movePlayer(1, movePlayerRed);
        });

        Button movePlayerGreen = new Button("Move Player Green");
        movePlayerGreen.addClassName("controlsButton");
        movePlayerGreen.addClickListener(event -> {
            movePlayer(2, movePlayerGreen);
        });

        Button movePlayerBlue = new Button("Move Player Blue");
        movePlayerBlue.addClassName("controlsButton");
        movePlayerBlue.addClickListener(event -> {
            movePlayer(3, movePlayerBlue);
        });

        Button movePlayerYellow = new Button("Move Player Yellow");
        movePlayerYellow.addClassName("controlsButton");
        movePlayerYellow.addClickListener(event -> {
            movePlayer(4, movePlayerYellow);
        });

        controls.add(save, load, movePlayerRed, movePlayerGreen, movePlayerYellow, movePlayerBlue);
        add(controls);
    }

    public void movePlayer(int playerIndex, Button button) {
        this.component.movePlayer(playerIndex, button);

    }
}





