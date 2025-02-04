package com.example.frontend;


import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.frontend", "com.example.shared"})
@Theme(value = "monopoly", variant = Lumo.DARK)
public class FrontendApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }

}
