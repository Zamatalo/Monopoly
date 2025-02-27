package com.example.application.components;


import com.example.application.dto.PlayerDTO;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.internal.AllowInert;
import lombok.Setter;
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
    private final Map<UUID, PlayerDTO> players = new HashMap<>();
    @Setter
    private String componentState;

    //TODO React navite, Vaadin Hilla, archetype
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setId("GameBoardComponent");
        getElement().executeJs("window.init($0);", this.componentState)
                .then(e -> {
                });
    }

    public void addPlayer(PlayerDTO player) {

    }

    public void removePlayer(PlayerDTO player) {

    }

    @ClientCallable
    public void movePlayer(String name, Button button) {
        getElement().executeJs("window.movePlayer($0, $1);", button.getElement(), name);
    }


    public PlayerDTO getPlayer() {
        return null;
    }

    public void saveGameState() {
        getElement().executeJs("return window.saveState();")
                .then(result -> {
                    componentState = result.toJson();
                    System.out.println("Saved game state: " + componentState);
                });
    }

    @ClientCallable
    @AllowInert
    public void loadGameState() {
        if (componentState != null) {
            System.out.println("Loading game state: " + componentState);
            getElement().executeJs("window.loadState($0);", this.componentState)
                    .then(e -> log.info(e.toString()));
        } else {
            System.out.println("Component state is null, cannot load game state.");
        }
    }

}
