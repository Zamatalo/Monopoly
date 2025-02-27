package com.example.application.views;

import com.example.application.PlayerNames;
import com.example.application.components.GameBoardComponent;
import com.example.application.dto.GameDTO;
import com.example.application.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AccessDeniedErrorRouter;
import lombok.SneakyThrows;

import java.util.UUID;


@Route("game")
@AccessDeniedErrorRouter(rerouteToError = NotFoundException.class)
@PreserveOnRefresh
public class GameView extends VerticalLayout implements HasUrlParameter<String>, BeforeEnterObserver {
    private final GameService gameService;
    private final GameBoardComponent component = new GameBoardComponent();
    private UUID gameId;

    public GameView(GameService gameService) {
        setSizeFull();
        initButtons();
        add(component);
        this.gameService = gameService;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            gameId = UUID.fromString(parameter);
            // loadGame(gameId);
        }
    }


    public void initButtons() {
        HorizontalLayout controls = new HorizontalLayout();
        controls.setId("controls");

        Button save = new Button("Save Game", _ -> {
            component.saveGameState();
        });
        save.addClassName("controlsButton");
        save.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button load = new Button("Load Game", _ -> {
            loadGame(gameId);
            component.loadGameState();
        });
        load.addClassName("controlsButton");
        load.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button movePlayerRed = new Button("Move Player Red");
        movePlayerRed.addClassName("controlsButton");
        movePlayerRed.addClickListener(event -> {
            movePlayer(PlayerNames.PLAYER_RED.toString(), movePlayerRed);
        });

        Button movePlayerGreen = new Button("Move Player Green");
        movePlayerGreen.addClassName("controlsButton");
        movePlayerGreen.addClickListener(event -> {
            movePlayer(PlayerNames.PLAYER_GREEN.toString(), movePlayerGreen);
        });

        Button movePlayerBlue = new Button("Move Player Blue");
        movePlayerBlue.addClassName("controlsButton");
        movePlayerBlue.addClickListener(event -> {
            movePlayer(PlayerNames.PLAYER_BLUE.toString(), movePlayerBlue);
        });

        Button movePlayerYellow = new Button("Move Player Yellow");
        movePlayerYellow.addClassName("controlsButton");
        movePlayerYellow.addClickListener(event -> {
            movePlayer(PlayerNames.PLAYER_YELLOW.toString(), movePlayerYellow);
        });

        controls.add(save, load, movePlayerRed, movePlayerGreen, movePlayerYellow, movePlayerBlue);
        add(controls);
    }

    private void movePlayer(String name, Button button) {
        this.component.movePlayer(name, button);

    }

    @SneakyThrows
    private void loadGame(UUID gameId) {
        GameDTO dto = gameService.findGameById(gameId); //refetch dto(later should be subscription(
        component.setComponentState(new ObjectMapper().writeValueAsString(dto));
        component.loadGameState();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        loadGame(gameId);
    }
}





