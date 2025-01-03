package com.example.shared.exeption;

import java.util.UUID;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(UUID gameId) {
        super("Game not found with id: " + gameId);
    }
}
