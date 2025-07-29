//package com.example.application.handlers.lobby;
//
//import com.example.application.entity.Player;
//import com.example.application.services.reactive.GameService_Mono;
//import com.example.application.services.imperative.PlayerService;
//import com.example.application.services.reactive.RedisService_Mono;
//import com.example.application.util.enums.PlayerColors;
//import com.example.application.utility.GameActionHandler;
//import com.example.application.utility.RequestContextRedis;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class JoinToGame_Handler implements GameActionHandler {
//    private final GameService_Mono gameService;
//    private final PlayerService playerService;
//    private final RedisService_Mono redisService;
//
//    @Override
//    public String getAction() {
//        return "JOIN_TO_GAME";
//    }
//
//    @Override
//    public void handle(RequestContextRedis ctx) {
//        try {
//            var gameId = UUID.fromString(ctx.body().get("gameId"));
//            var playerId = UUID.fromString(ctx.body().get("playerId"));
//            var playerName = ctx.body().get("playerName");
//            var playerColor = PlayerColors.valueOf(ctx.body().get("playerColor"));
//
//            if (!playerService.existsById(playerId)) {
//                Player newPlayer = new Player();
//                newPlayer.setPlayerId(playerId);
//                newPlayer.setPlayerName(playerName);
//                newPlayer.setColor(playerColor);
//
//                var updatedGame = gameService.addPlayerToGame_Mono(newPlayer, gameId);
//
//                redisService.publishGameUpd(gameService.findById_Mono(gameId));
//                ctx.respond(updatedGame);
//                return;
//            }
//
//            ctx.respond("Player already exists.");
//        } catch (Exception e) {
//            ctx.respond("Internal Server Error");
//            log.error("Error in handle(): {}", e.getMessage(), e);
//        }
//    }
//
//}
