package com.example.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AccessDeniedErrorRouter;

import java.util.UUID;

@Route("main")
@RouteAlias("")
@AccessDeniedErrorRouter(rerouteToError = NotFoundException.class)
public class Main extends VerticalLayout {
    private final Dialog dialog = new Dialog();

    public Main() {
        addClassName("main");
        Button connectButton = new Button("Connect to a game");
        connectButton.addClickListener(_ -> {
            dialog.open();
        });

        Button createNewGameButton = new Button("Create new game");
        createNewGameButton.addClickListener(_ -> {

        });
        dialogInit();
        add(connectButton, createNewGameButton);
    }

    private void dialogInit() {
        FormLayout formLayout = new FormLayout();
        TextField gameIdField = new TextField("Game ID");

        Button connect = new Button("Connect", _ -> {
            String gameId = gameIdField.getValue();
            if (!gameId.isEmpty()) {
                dialog.close();
                getUI().ifPresent(ui -> ui.navigate(GameView.class, gameId));
            }
        });

        formLayout.add(gameIdField, connect);
        dialog.add(formLayout);
    }


    private void createNewGame() {
        UUID gameId = UUID.randomUUID();
        getUI().ifPresent(ui -> ui.navigate("game/" + gameId));
    }

}
