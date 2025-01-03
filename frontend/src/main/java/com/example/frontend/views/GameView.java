package com.example.frontend.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.awt.*;

@Route("GameView")
@RouteAlias("")
public class GameView extends VerticalLayout {
    public GameView() {
        GridLayout gameBoard = new GridLayout(10, 10);
        gameBoard.setS();

        for (int i = 0; i < 40; i++) {
            Button cell = new Button("Cell " + i);
            cell.addClickListener(e -> Notification.show("Clicked: " + i));
            gameBoard.add(cell);
        }

        HorizontalLayout playerInfo = new HorizontalLayout();
        playerInfo.add(new Label("Player 1: $1500"));
        playerInfo.add(new Label("Player 2: $1500"));

        add(gameBoard, playerInfo);
    }
}
