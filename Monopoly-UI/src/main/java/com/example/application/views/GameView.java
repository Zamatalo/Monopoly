package com.example.application.views;

import com.example.application.components.GameBoardComponent;
import com.example.application.service.GameService;
import com.example.application.types.GameDTO;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AccessDeniedErrorRouter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Route("game")
@AccessDeniedErrorRouter(rerouteToError = NotFoundException.class)
@PreserveOnRefresh
public class GameView extends VerticalLayout implements HasUrlParameter<String> {
    private final GameService gameService;
    private UUID gameId;
    private GameBoardComponent gameBoardComponent;
    private Disposable subscription;

    public GameView(GameService gameService) {
        setSizeFull();
        this.gameService = gameService;
        initButtons();
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            gameId = UUID.fromString(parameter);
            loadGame(gameId);
        }
    }

    private void initButtons() {
        HorizontalLayout controls = new HorizontalLayout();
        controls.setId("controls");

        Button load = new Button("Load Game", _ -> loadGame(gameId));
        Button rollDice = new Button("Roll dice", _ -> gameService.rollDice(gameId));

        load.addClassName("controlsButton");
        load.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        controls.add(load, rollDice);
        add(controls);
    }

    private void loadGame(UUID gameId) {
        if (subscription != null) {
            subscription.dispose();
        }

        if (gameBoardComponent != null) {
            remove(gameBoardComponent);
        }

        gameBoardComponent = new GameBoardComponent();
        add(gameBoardComponent);

        GameDTO gameDTO = gameService.findGameById(gameId);
        gameBoardComponent.updateGame(gameDTO);

        subscription = gameService.subscribeToGame(gameId).subscribe(
                game -> {
                    Optional<UI> ui = getUI();
                    if (ui.isPresent()) {
                        ui.get().access(() -> {
                            log.info("Game update received: {}", game);
                            gameBoardComponent.updateGame(game);
                        });
                    } else {
                        log.warn("UI is null. Cannot update game state.");
                    }
                },
                error -> {
                    UI ui = UI.getCurrent();
                    if (ui != null) {
                        ui.access(() -> {
                            Notification.show("Error loading game: " + error.getMessage(), 1, Notification.Position.BOTTOM_CENTER);
                            log.error("Subscription error: {}", error.getMessage());
                        });
                    } else {
                        log.error("UI is null. Cannot show error notification.");
                    }
                }
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (subscription != null) {
            subscription.dispose();
        }
        super.onDetach(detachEvent);
    }
}