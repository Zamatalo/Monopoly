//package com.example.application.handlers.lobby;
//
//import com.example.application.services.reactive.GameService_Mono;
//import com.example.application.services.reactive.RedisService_Mono;
//import com.example.application.services.TurnService;
//import com.example.application.utility.GameActionHandler;
//import com.example.application.utility.RequestContextRedis;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class EndTurn_Handler implements GameActionHandler {
//    private final TurnService turnService;
//    private final GameService_Mono gameService;
//    private final RedisService_Mono redisService;
//
//    @Override
//    public String getAction() {
//        return "END_TURN";
//    }
//
//    @Override
//    public void handle(RequestContextRedis ctx) {
//        try {
//            UUID gameId = UUID.fromString(ctx.body().get("gameId"));
//
//
//            var updatedGame = gameService.findById_Mono(gameId);
//            redisService.publishGameUpd(updatedGame);
//            turnService.endTurn(gameId);
//            ctx.respond(updatedGame);
//        } catch (Exception e) {
//            ctx.respond("Internal Server Error");
//            log.error(e.getMessage());
//        }
//    }
//}