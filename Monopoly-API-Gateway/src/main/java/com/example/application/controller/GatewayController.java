package com.example.application.controller;

import com.example.application.components.publishers.GamePublisher;
import com.example.application.service.GameGatewayService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerColors;
import com.example.application.types.PlayerDTO;
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
    private final GamePublisher gamePublisher;

    @MutationMapping
    public GameDTO createNewGame() {
        String json = redisRequestReplyService.sendAction("CREATE_GAME").join();
        try {
            return mapper.readValue(json, GameDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @QueryMapping
    public List<GameDTO> getAllGames() {
        String json = redisRequestReplyService.sendAction("getAllGames").join();
        try {
            return Arrays.asList(mapper.readValue(json, GameDTO[].class));
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
            return mapper.readValue(json, GameDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @QueryMapping
    public GameDTO findGameById(@Argument("gameId") UUID gameId) {
        String json = redisRequestReplyService.sendAction(
                        "findGameById",
                        Map.of("gameId", gameId.toString()))
                .join();
        try {
            return mapper.readValue(json, GameDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @MutationMapping
    public GameDTO joinToGame(@Argument("gameId") UUID gameId,
                              @Argument("playerName") String playerName,
                              @Argument("playerColor") PlayerColors playerColor,
                              @Argument("playerId") UUID playerId) {

        String json = redisRequestReplyService.sendAction(
                        "JOIN_TO_GAME",
                        Map.of("gameId", gameId.toString(),
                                "playerName", playerName,
                                "playerColor", playerColor.toString(),
                                "playerId", playerId.toString()
                        ))
                .join();
        try {
            var game = mapper.readValue(json, GameDTO.class);
            gamePublisher.publish(game);
            return game;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @MutationMapping
    public GameDTO addBotToGame(@Argument("gameId") UUID gameId) {
        String json = redisRequestReplyService.sendAction(
                        "ADD_BOT",
                        Map.of("gameId", gameId.toString()))
                .join();
        try {
            var game = mapper.readValue(json, GameDTO.class);
            gamePublisher.publish(game);
            return game;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @QueryMapping
    public PlayerDTO getPlayer(@Argument("playerId") UUID playerId) {
        String json = redisRequestReplyService.sendAction(
                        "getPlayer",
                        Map.of("playerId", playerId.toString()))
                .join();
        try {
            return mapper.readValue(json, PlayerDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }

    @MutationMapping
    public GameDTO startGame(@Argument("gameId") UUID gameId) {
        String json = redisRequestReplyService.sendAction(
                        "START_GAME",
                        Map.of("gameId", gameId.toString()))
                .join();
        try {
            var game = mapper.readValue(json, GameDTO.class);
            gamePublisher.publish(game);
            return game;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse GameDTO list from Redis response", e);
        }
    }
}
