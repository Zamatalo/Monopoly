package com.example.application.handlers.game;

import com.example.application.services.GameService;
import com.example.application.util.enums.GameActions;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GetGameActions_Handler implements GameActionHandler {
    private final GameService gameService;
    private final ObjectMapper objectMapper;

    @Override
    public String getAction() {
        return "getGameActions";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            UUID gameId = UUID.fromString(ctx.body().get("gameId"));
            List<GameActions> actions = gameService.resolveGameActions(gameId);
            ctx.respond(objectMapper.writeValueAsString(actions));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
