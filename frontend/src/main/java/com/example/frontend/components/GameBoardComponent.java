package com.example.frontend.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.NpmPackage;


@JavaScript("./src/monopoly.js")
@NpmPackage(value = "three", version = "0.172.0")
@Tag("canvas")
public class GameBoardComponent extends Component {

    public GameBoardComponent() {
        getElement().executeJs("window.initThree($0)", this);

    }

}
