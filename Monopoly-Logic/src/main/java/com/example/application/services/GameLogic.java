package com.example.application.services;

import com.example.application.components.GameSubscription;

import java.util.random.RandomGenerator;

public class GameLogic {
    private final GameSubscription gameSubscription;

    public GameLogic(GameSubscription gameSubscription) {
        this.gameSubscription = gameSubscription;
    }

    public int roll() {
        return RandomGenerator.getDefault().nextInt(1, 6);
    }
}
