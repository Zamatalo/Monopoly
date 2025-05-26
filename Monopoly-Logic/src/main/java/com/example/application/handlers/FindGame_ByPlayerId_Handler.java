package com.example.application.handlers;

import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindGame_ByPlayerId_Handler implements GameActionHandler {
    private final PlayerService playerService;
    private final ObjectMapper objectMapper;

    @Override
    public String getAction() {
        return "findGameByPlayerId";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var playerId = ctx.body().get("playerId");
            var player = playerService.findById(UUID.fromString(playerId));
            var game = player.get().getGame();
            var payload = objectMapper.writeValueAsString(game);

            ctx.respond(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
