package com.example.frontend.views;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route("game")
@RouteAlias("")
@JavaScript("./src/monopoly.js")
@NpmPackage(value = "three", version = "0.172.0")
@Tag("canvas")
public class GameView extends VerticalLayout {

    public GameView() {
        Div threeJsContainer = new Div();
        threeJsContainer.setId("threejs-container");
        threeJsContainer.setWidth("100px");
        threeJsContainer.setHeight("100%");


        threeJsContainer.getElement().executeJs("window.initThree($0)", this);
        add(threeJsContainer);
    }

}
