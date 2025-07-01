package com.example.application.controller;


import com.example.application.service.GameGatewayService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerColors;
import com.example.application.types.PlayerDTO;
import com.example.application.types.SpecialTileEffect;
import data.SpecialTileData;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/// #TODO: add proper exceptions, and handle futures. Also make more controllers
/// #TODO: if more then one game, then thread will be blocked, should find workaround
@RequiredArgsConstructor
@Controller
public class GatewayController {
    private final GameGatewayService redisRequestReplyService;

    @MutationMapping
    public GameDTO createNewGame() {
        return redisRequestReplyService.sendAction("CREATE_GAME", GameDTO.class).join();
    }

    @QueryMapping
    public List<GameDTO> getAllGames() {
        return List.of(redisRequestReplyService.sendAction("getAllGames", GameDTO[].class).join());
    }

    @QueryMapping
    public GameDTO findGameByPlayerId(@Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                        "findGameByPlayerId",
                        Map.of("playerId", playerId.toString()),
                        GameDTO.class)
                .join();
    }

    @QueryMapping
    public GameDTO findGameById(@Argument("gameId") UUID gameId) {
        return redisRequestReplyService.sendAction(
                        "findGameById",
                        Map.of("gameId", gameId.toString()),
                        GameDTO.class)
                .join();

    }

    @MutationMapping
    public GameDTO joinToGame(@Argument("gameId") UUID gameId,
                              @Argument("playerName") String playerName,
                              @Argument("playerColor") PlayerColors playerColor,
                              @Argument("playerId") UUID playerId) {

        return redisRequestReplyService.sendAction(
                        "JOIN_TO_GAME",
                        Map.of("gameId", gameId.toString(),
                                "playerName", playerName,
                                "playerColor", playerColor.toString(),
                                "playerId", playerId.toString()),
                        GameDTO.class)
                .join();
    }

    @MutationMapping
    public GameDTO addBotToGame(@Argument("gameId") UUID gameId) {
        return redisRequestReplyService.sendAction(
                        "ADD_BOT",
                        Map.of("gameId", gameId.toString()),
                        GameDTO.class)
                .join();
    }

    @QueryMapping
    public PlayerDTO getPlayer(@Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                        "getPlayer",
                        Map.of("playerId", playerId.toString()),
                        PlayerDTO.class)
                .join();

    }

    @MutationMapping
    public GameDTO startGame(@Argument("gameId") UUID gameId) {
        return redisRequestReplyService.sendAction(
                        "START_GAME",
                        Map.of("gameId", gameId.toString()),
                        GameDTO.class)
                .join();
    }

    @MutationMapping
    public Integer rollDice(
            @Argument("gameId") UUID gameId,
            @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                        "ROLL_DICE",
                        Map.of("gameId", gameId.toString(),
                                "playerId", playerId.toString()),
                        Integer.class)
                .join();
    }

    @MutationMapping
    public GameDTO buyPropertyForPlayer(@Argument("gameId") UUID gameId, @Argument("playerId") UUID playerId){
        return redisRequestReplyService.sendAction(
                        "BUY_PROPERTY",
                        Map.of("gameId", gameId.toString(),
                                "playerId", playerId.toString()),
                        GameDTO.class)
                .join();
    }

    @MutationMapping
    public GameDTO endTurn(@Argument("gameId") UUID gameId, @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                        "END_TURN",
                        Map.of("gameId", gameId.toString(),
                                "playerId", playerId.toString()),
                        GameDTO.class)
                .join();
    }

    @MutationMapping
    public SpecialTileData resolveSpecialTile(@Argument("gameId") UUID gameId, @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                        "SPECIAL_TILE",
                        Map.of("gameId", gameId.toString(),
                                "playerId", playerId.toString()),
                        SpecialTileData.class)
                .join();
    }
}
