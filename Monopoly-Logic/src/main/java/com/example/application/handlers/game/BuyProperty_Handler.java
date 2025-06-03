package com.example.application.handlers.game;

import com.example.application.services.*;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import com.example.application.util.PropertyData;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.GameMapper;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuyProperty_Handler implements GameActionHandler {
    private final GameService gameService;
    private final PlayerService playerService;
    private final RedisService redisService;
    private final TurnService turnService;

    @Override
    public String getAction() {
        return "BUY_PROPERTY";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var gameId = UUID.fromString(ctx.body().get("gameId"));
            var playerId = UUID.fromString(ctx.body().get("playerId"));

            GameDTO game = gameService.findById(gameId);
            PlayerDTO player = playerService.findById(playerId).orElseThrow();

            var whichCellIsPlayerStandingOn_Property = PropertyData.ofPos(player.getPosition());

            var allBoughtPropertiesForGame = gameService.findAllProperties(gameId);
            if (allBoughtPropertiesForGame.contains(whichCellIsPlayerStandingOn_Property)) {
                ctx.respond("Property already bought");
                throw new IllegalArgumentException("Property already bought");
            }

            playerService.addPropertyToPlayer(playerId,whichCellIsPlayerStandingOn_Property);
            redisService.publishGameUpd(gameService.findById(gameId));
            turnService.endTurn(gameId);
            if (!isFromBot(ctx)) {
                ctx.respond(gameService.findById(gameId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// if its from bot, record should be ack
    /// later should be dedicated channel for bot calls?
    public boolean isFromBot(RequestContextRedis ctx) {
        var a = ctx.body().get("isFromBot");
        return a != null && a.equals("true");
    }
}
