package com.example.application.controller;

import com.example.application.service.GameGatewayService;
import com.example.application.types.GameActions;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerColors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @QueryMapping
    public List<PlayerActions> getPlayerActions(@Argument("gameId") UUID gameId,
                                                @Argument("playerId") UUID playerId) {
        String json = redisRequestReplyService.sendAction("getPlayerActions",
                        Map.of("gameId", gameId.toString(),
                                "playerId",playerId.toString()))
                .join();
        try {
            String actualJson = mapper.readValue(json, String.class);
            return List.of(mapper.readValue(actualJson, PlayerActions[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @QueryMapping
    public List<GameActions> getGameActions(@Argument("gameId") UUID gameId) {
        String json = redisRequestReplyService.sendAction("getGameActions",
                        Map.of("gameId", gameId.toString()))
                .join();
        try {
            String actualJson = mapper.readValue(json, String.class);
            return List.of(mapper.readValue(actualJson, GameActions[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @MutationMapping
    public GameDTO createNewGame() {
        String json = redisRequestReplyService.sendAction("createNewGame").join();
        try {
            String actualJson = mapper.readValue(json, String.class);
            return mapper.readValue(actualJson, GameDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @QueryMapping
    public List<GameDTO> getAllGames() {
        String json = redisRequestReplyService.sendAction("getAllGames").join();
        try {
            String actualJson = mapper.readValue(json, String.class);
            return Arrays.asList(mapper.readValue(actualJson, GameDTO[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @QueryMapping
    public GameDTO findGameByPlayerId(@Argument("playerId") UUID playerId) {
        String json = redisRequestReplyService.sendAction(
                        "findGameByPlayerId",
                        Map.of("playerId", playerId.toString()))
                .join();
        try {
            String actualJson = mapper.readValue(json, String.class);
            return mapper.readValue(actualJson, GameDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @QueryMapping
    public GameDTO findGameById(@Argument("gameId") UUID gameId) {
        String json = redisRequestReplyService.sendAction("findGameById",
                        Map.of("gameId", gameId.toString()))
                .join();
        try {
            String actualJson = mapper.readValue(json, String.class);
            return mapper.readValue(actualJson, GameDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @MutationMapping
    public GameDTO joinToGame(@Argument("gameId") UUID gameId,
                              @Argument("playerName") String playerName,
                              @Argument("playerColor") PlayerColors playerColor,
                              @Argument("playerId") UUID playerId) {

        String json = redisRequestReplyService.sendAction("joinToGame",
                        Map.of("gameId", gameId.toString(),
                                "playerName", playerName,
                                "playerColor", playerColor.toString(),
                                "playerId", playerId.toString()
                        ))
                .join();
        try {
            String actualJson = mapper.readValue(json, String.class);
            return mapper.readValue(actualJson, GameDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }
}
