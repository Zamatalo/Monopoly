package com.example.application;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication()
@EntityScan(basePackages = {"com.example.application.entity"})
@EnableJpaRepositories(basePackages = "com.example.application.repo")
public class LogicApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogicApplication.class, args);
    }
}