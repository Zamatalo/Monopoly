package com.example.application.handlers.lobby;

import com.example.application.services.GameService;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class FindGameById_Handler implements GameActionHandler {
    private final GameService gameService;
    private final ObjectMapper objectMapper;

    @Override
    public String getAction() {
        return "findGameById";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var gameId = ctx.body().get("gameId");
            var game = gameService.findById(UUID.fromString(gameId));
            String payload = objectMapper.writeValueAsString(game);

            ctx.respond(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
