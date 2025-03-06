package com.example.application.components;

import com.example.application.types.GameDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.NpmPackage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JavaScript("./src/Main.js")
@NpmPackage(value = "three", version = "0.172.0")
@NpmPackage(value = "gsap", version = "3.12.7")
@Tag("div")
public class GameBoardComponent extends Component implements HasSize, HasStyle {

    @SneakyThrows
    public GameBoardComponent() {
        setId("GameBoardComponent");
        getElement().executeJs("window.init($0);", getElement())
                .then(_ -> log.info("Three.js scene initialized"));
    }

    @SneakyThrows
    public void updateGame(GameDTO gameDTO) {
        String gameJson = new ObjectMapper().writeValueAsString(gameDTO);
        getElement().executeJs("window.updateGame($0);", gameJson)
                .then(_ -> log.info("Game state updated: {}", gameDTO.getGameId()));
    }


    @Override
    protected void onDetach(DetachEvent detachEvent) {
        getElement().executeJs("window.cleanup();")
                .then(_ -> log.info("Three.js resources cleaned up"));
        super.onDetach(detachEvent);
    }
}