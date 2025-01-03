package com.example.shared.exeption;

import java.util.UUID;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(UUID gameId) {
        super("Player not found with id: " + gameId);
    }
}
