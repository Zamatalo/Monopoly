package com.example.application.handlers.lobby;

import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RollDice_Handler implements GameActionHandler {
    private final GameService gameService;
    private final PlayerService playerService;

    @Override
    public String getAction() {
        return "ROLL_DICE";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var gameId = ctx.body().get("gameId");
            var playerId = ctx.body().get("playerId");

            Optional<GameDTO> game = gameService.findById(UUID.fromString(gameId));
            assert game.isPresent();
            Optional<PlayerDTO> playerDTO = playerService.findById(UUID.fromString(playerId));
            assert playerDTO.isPresent();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
