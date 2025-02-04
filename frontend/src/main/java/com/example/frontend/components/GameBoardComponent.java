package com.example.frontend.components;

import com.example.shared.model.player.Player;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.NpmPackage;
import elemental.json.JsonValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@JavaScript("./src/Main.js")
@NpmPackage(value = "three", version = "0.172.0")
@NpmPackage(value = "gsap", version = "3.12.7")
@Tag("div")
public class GameBoardComponent extends Component implements HasSize, HasStyle {
    private final Map<UUID, Player> players = new HashMap<>();

    @Getter
    private JsonValue componentState;
    public GameBoardComponent() {
        setId("GameBoardComponent");
//        setHeight("100%");
//        setWidth("100%");
        UI.getCurrent().access(() -> {
            getElement().executeJs("window.init();");
        });
    }

    public void addPlayer(Player player) {

    }

    public void removePlayer(Player player) {

    }

    @ClientCallable
    public void movePlayer(int playerId, Button button) {
        getElement().executeJs("window.movePlayer($0,$1);", playerId, button)
                .then(result -> {
                    log.info("Player {} moved", playerId);
        });
    }

    public Player getPlayer() {
        return null;
    }

    public void saveGameState() {
        getElement().executeJs("return window.saveState();")
                .then(result -> {
                    componentState = result;
                    log.info("Saved GameState: {}", componentState.toJson());
                });
    }

    public void loadGameState() {
        if (componentState != null) {
            getElement().executeJs("window.loadState($0);", this.componentState)
                    .then(result -> log.info("GameBoard loaded"));
        }
    }
}
