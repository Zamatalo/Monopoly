package com.example.application.controller;

import com.example.application.service.GameGatewayService;
import com.example.application.types.GameDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class GatewayController {
    private final GameGatewayService redisRequestReplyService;
    private final ObjectMapper mapper;

//    @MutationMapping
//    public void makePlayerAction(@Argument("gameId") UUID gameId,
//                                 @Argument("playerId") UUID playerId,
//                                 @Argument("action") PlayerActions action) {
//        publisher.publish("gameId:" + gameId + ":player:" + playerId + ":action", action);
//    }
//
//    @MutationMapping
//    public void makeGameAction(@Argument("gameId") UUID gameId,
//                               @Argument("action") GameActions action) {
//        publisher.publish("gameId:" + gameId + ":action", action);
//    }

//    @QueryMapping
//    public CompletableFuture<List<GameActions>> getGameActions(@Argument("gameId") UUID gameId) {
//        return redisRequestReplyService.sendAndReceive(
//                "game:"+gameId+":getActions",
//                Map.of("gameId", gameId), GameActions.class);
//    }
//
//    @QueryMapping
//    public CompletableFuture<List<PlayerActions>> getPlayerActions(@Argument("gameId") UUID gameId,
//                                                             @Argument("playerId") UUID playerId) {
//        return redisRequestReplyService.sendAndReceive(
//                "game:"+gameId+":player:"+playerId+":getActions",
//                Map.of("gameId", gameId, "playerId", playerId), PlayerActions.class
//        );
//    }

    @QueryMapping
    public List<GameDTO> getAllGames() {
        String json =redisRequestReplyService.getAllGames().join();
        try {
            String actualJson = mapper.readValue(json, String.class);
            return Arrays.asList(mapper.readValue(actualJson, GameDTO[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }
    @QueryMapping
    public GameDTO findGameByPlayerId(@Argument("playerId") UUID playerId) {
        String json = redisRequestReplyService.getGame_PlayerId(playerId.toString()).join();
        try {
            return mapper.readValue(json, GameDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }
}
