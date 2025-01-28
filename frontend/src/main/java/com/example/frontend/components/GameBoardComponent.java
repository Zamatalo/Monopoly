package com.example.frontend.components;

import com.example.shared.model.player.Player;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
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
@Tag("canvas")
public class GameBoardComponent extends Component implements HasSize, HasStyle {
    private final Map<UUID, Player> players = new HashMap<>();

    @Getter
    private JsonValue componentState;

    public GameBoardComponent() {
        setId("GameBoardComponent");
        getElement().executeJs("window.init($0)", this);
    }

    public void addPlayer(Player player) {

    }

    public void removePlayer(Player player) {

    }

    public void movePlayer(int playerId, int pos) {
        getElement().executeJs("window.movePlayer($0,$1);", playerId, pos).then(result -> {
            log.info("Player " + playerId + " moved");
        });
    }

    public Player getPlayer() {
        return null;
    }

    public void saveGameState() {
        getElement().executeJs("return window.saveState();")
                .then(result -> {
                    componentState = result;
                    log.info("Saved GameState: " + componentState.toJson());
                });
    }

    public void loadGameState() {
        if (componentState != null) {
            getElement().executeJs("window.loadState($0);", this.componentState)
                    .then(result -> log.info("GameBoard loaded"));
        }
    }
}
