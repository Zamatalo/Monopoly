package com.example.application;


import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
@Theme("my-theme")
//@Theme(value = "monopoly", variant = Lumo.DARK)
public class FrontendApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }

}
