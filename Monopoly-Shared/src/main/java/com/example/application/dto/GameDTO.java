package com.example.application.dto;

import com.example.application.GameState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDTO {
    private UUID gameId;
    private GameState gameState;
    private List<PlayerDTO> players;
    private int currentPlayerIndex;
}
