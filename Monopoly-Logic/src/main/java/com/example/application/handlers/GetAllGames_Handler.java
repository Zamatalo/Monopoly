package com.example.application.handlers;

import com.example.application.services.GameService;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GetAllGames_Handler implements GameActionHandler {
    private final GameService gameService;
    private final ObjectMapper objectMapper;

    @Override
    public String getAction() {
        return "getAllGames";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var games = gameService.findAll();
            String payload = objectMapper.writeValueAsString(games);
            ctx.respond(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
